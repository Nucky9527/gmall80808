package com.atguigu.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParams;
import com.atguigu.gmall.bean.SkuLsResult;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.Update;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Service
public class ListServiceImpl implements ListService {

    public static final String ES_INDEX = "gmall";

    public static final String ES_TYPE = "SkuInfo";

    @Autowired
    private RedisUtil redisUtil;


    /**
     * 将数据库中的重要字段{封装到skuLsInfo}保存到es中
     *
     * @param skuLsInfo
     */
    // 我们要将数据保存到es 中，所以此处应该引用操作es客户端的对象
    @Autowired
    private JestClient jestClient;

    @Override
    public void saveSkuInfo(SkuLsInfo skuLsInfo) {

        // 保存的功能 put /gmall/SkuInfo/1
        Index index = new Index.Builder(skuLsInfo).index(ES_INDEX).type(ES_TYPE).id(skuLsInfo.getId()).build();

        try {
            jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据用户传递过来的参数进行查询数据！
     *
     * @param skuLsParam
     * @return
     */
    @Override
    public SkuLsResult search(SkuLsParams skuLsParam) {
        // 获取dsl语句
        String query = makeQueryStringForSearch(skuLsParam);

        // 准备执行
        Search search = new Search.Builder(query).addIndex(ES_INDEX).addType(ES_TYPE).build();
        SearchResult searchResult = null;
        try {
            searchResult = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //  searchResult 转换为 SkuLsResult
        SkuLsResult skuLsResult = makeResultForSearch(skuLsParam, searchResult);
        return skuLsResult;
    }


    private SkuLsResult makeResultForSearch(SkuLsParams skuLsParam, SearchResult searchResult) {
        SkuLsResult skuLsResult = new SkuLsResult();
//      List<SkuLsInfo> skuLsInfoList;
        // 声明一个集合来存储SkuLsInfo
        ArrayList<SkuLsInfo> skuLsInfoList = new ArrayList<>();
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
        // 循环遍历
        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
            SkuLsInfo skuLsInfo = hit.source;
            // 因为我们要取得高亮的skuName
            if (hit.highlight != null && hit.highlight.size() > 0) {
                // 取得里面的数据 ,取出skuName 的高亮集合
                List<String> list = hit.highlight.get("skuName");
                // 取得第一条数据即可！ 因为通过key = skuName ，只能取得一条value 值。
                String skuNameHl = list.get(0);
                // 将skuName 名称进行替换
                skuLsInfo.setSkuName(skuNameHl);
            }
            skuLsInfoList.add(skuLsInfo);
        }

        // 将es 中的skuLsInfo 集合添加到 skuLsResult 对象中！
        skuLsResult.setSkuLsInfoList(skuLsInfoList);
//        long total;
        skuLsResult.setTotal(searchResult.getTotal());
//        long totalPages;
        // 求出总页数 10  3  4  |  9  3  3
        // long pages = searchResult.getTotal()%skuLsParam.getPageSize()==0?searchResult.getTotal()/skuLsParam.getPageSize():(searchResult.getTotal()/skuLsParam.getPageSize())+1;
        // 因为实践中的出来的理论！
        long pages = (searchResult.getTotal() + skuLsParam.getPageSize() - 1) / skuLsParam.getPageSize();
        skuLsResult.setTotalPages(pages);
//        List<String> attrValueIdList;
//        声明一个存储平台属性值的集合
        ArrayList<String> attrValueIdList = new ArrayList<>();
//        获取平台属性值Id aggregations
        MetricAggregation aggregations = searchResult.getAggregations();
        TermsAggregation groupby_attr = aggregations.getTermsAggregation("groupby_attr");
        List<TermsAggregation.Entry> buckets = groupby_attr.getBuckets();
        if (buckets != null && buckets.size() > 0) {
            // 循环取值
            for (TermsAggregation.Entry bucket : buckets) {
                String valueId = bucket.getKey();
                // 将valueId 添加到集合中
                attrValueIdList.add(valueId);
            }
        }
        // 平台属性值Id集合
        skuLsResult.setAttrValueIdList(attrValueIdList);

        return skuLsResult; //
    }

    /**
     * 基于事先写好的dsl语句来完成
     *
     * @param skuLsParam
     * @return
     */
    private String makeQueryStringForSearch(SkuLsParams skuLsParam) {
        // 创建查询的构造器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // skuName-->keyword 匹配 ，高亮
        if (skuLsParam.getKeyword() != null && skuLsParam.getKeyword().length() > 0) {
            //  {"match": { "skuName": "小米" }}
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", skuLsParam.getKeyword());
            // "bool": { "must" }
            boolQueryBuilder.must(matchQueryBuilder);
            // 设置一个高亮
            HighlightBuilder highlighter = searchSourceBuilder.highlighter();
            highlighter.preTags("<span style='color:red'>");
            highlighter.field("skuName");
            highlighter.postTags("</span>");
            // 将设置好的高亮对象放入到查询构造器中！
            searchSourceBuilder.highlight(highlighter);
        }

        // 过滤 skuAttrValueList.valueId catalog3Id
        if (skuLsParam.getCatalog3Id() != null && skuLsParam.getCatalog3Id().length() > 0) {
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", skuLsParam.getCatalog3Id());
            boolQueryBuilder.filter(termQueryBuilder);
        }
        // 集合长度：size(); 数组长度：length 字符串：length() 文件长度：length();
        if (skuLsParam.getValueId() != null && skuLsParam.getValueId().length > 0) {
            // 循环遍历
            for (String valueId : skuLsParam.getValueId()) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", valueId);
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }
        // query :{ bool }
        searchSourceBuilder.query(boolQueryBuilder);

        // 从第几条数据开始查询 pageSize * (pageNo-1);
        int from = (skuLsParam.getPageNo() - 1) * skuLsParam.getPageSize();
        searchSourceBuilder.from(from);
        // 每页显示的数据条数
        searchSourceBuilder.size(skuLsParam.getPageSize());

        // 排序
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);

        // 集合
        TermsBuilder groupby_attr = AggregationBuilders.terms("groupby_attr").field("skuAttrValueList.valueId");
        searchSourceBuilder.aggregation(groupby_attr);
        // 返回dsl的query语句
        String query = searchSourceBuilder.toString();
        System.out.println("query:" + query);
        return query;
    }


    /**
     * 通过hotscore更新热度评分
     *
     * @param skuId
     */
    @Override
    public void incrHotScore(String skuId) {

        Jedis jedis = redisUtil.getJedis();
        //当记录到10次的时候，在es中存一次
        int timeToEs = 10;
        Double hotScore = jedis.zincrby("hotScore", 1, "skuId" + skuId);
        if (hotScore % timeToEs == 0) {
            updateHotScore(skuId, Math.round(hotScore));
        }
    }

    /**
     * 更新es
     */
    private void updateHotScore(String skuId, Long hotScore) {
        String updateJson = "{\n" +
                "   \"doc\":{\n" +
                "     \"hotScore\":" + hotScore + "\n" +
                "   }\n" +
                "}";

        Update update = new Update.Builder(updateJson).index("gmall").type("SkuInfo").id(skuId).build();

        try {
            jestClient.execute(update);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}





