package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.enums.ProcessStatus;
import com.atguigu.gmall.config.ActiveMQUtil;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.util.HttpClientUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {


    @Autowired
    private OrderInfoMapper  orderInfoMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ActiveMQUtil activeMQUtil;


    @Override
    public String saveOrder(OrderInfo orderInfo) {

        //设置创建时间
        orderInfo.setCreateTime(new Date());
        //设置失效时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,1);
        orderInfo.setExpireTime(calendar.getTime());
        //生成第三方支付编号
        String outTradeNo="ATGUIGU"+System.currentTimeMillis()+""+new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        //插入订单详细信息
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }
        //为了跳转到支付页面使用，支付会根据订单id进行支付
        String orderId = orderInfo.getId();
        return orderId;


    }


    //生成流水号
    public  String getTradeNo(String userId){
        Jedis jedis = redisUtil.getJedis();
        //定义key
        String tradeNoKey = "user:"+userId+":tradeCode";
        //指定的key对应生成一个code
        String tradeCode = UUID.randomUUID().toString();
        jedis.setex(tradeNoKey,10*60,tradeCode);
        jedis.close();
        return tradeCode;
    }

    //验证流水号
    public boolean checkTradeCode(String userId,String tradeCodeNo){
        Jedis jedis = redisUtil.getJedis();
        //定义key
        String tradeNoKey = "user:"+userId+":tradeCode";
        //指定key拿到对应的code
        String tradeCode = jedis.get(tradeNoKey);
        jedis.close();
        //判断code是否相等，如果相等，那么return true
        if(tradeCode!=null && tradeCode.equals(tradeCodeNo)){
            return true;
        }else{
            return false;
        }
    }

    //删除流水号
    public  void  delTradeCode(String userId){
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey = "user:"+userId+":tradeCode";
        jedis.del(tradeNoKey);
        jedis.close();
    }


    /**
     * 验证库存数量
     * @param skuId
     * @param skuNum
     * @return
     */
    @Override
    public boolean checkStock(String skuId, Integer skuNum) {
        String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + skuId + "&num=" + skuNum);
        //判断返回的结果
        if("1".equals(result)){
            return  true;
        }else{
            return  false;
        }

    }


    /**
     * 通过orderId，获得指定的列表信息
     * @param orderId
     * @return
     */
    @Override
    public OrderInfo getOrderInfo(String orderId) {

        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);

        return orderInfo;

    }

    /**
     * 通知过来之后，订单作为消费者，消费消息，并且根据订单编号更新订单状态
     * @param orderId
     * @param processStatus
     */
    @Override
    public void updateOrderStatus(String orderId, ProcessStatus processStatus) {

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setProcessStatus(processStatus);
        orderInfo.setOrderStatus(processStatus.getOrderStatus());
        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);


    }

    /**
     *根据订单id，发送消息给库存！
     * @param orderId
     */
    @Override
    public void sendOrderStatus(String orderId) {
        //得到链接
        Connection connection = activeMQUtil.getConnection();
        //根据orderId将orderInfo的数据转换为json字符串
    String orderJson =   initWareOrder(orderId);
    //打开链接
        try {
            connection.start();
            //创建session
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            //创建队列
            Queue order_result_quene = session.createQueue("ORDER_RESULT_QUEUE");
            //创建消息提供者
            MessageProducer producer = session.createProducer(order_result_quene);
            ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
            activeMQTextMessage.setText(orderJson);
            //准备发送消息
            producer.send(activeMQTextMessage);
            //提交
            session.commit();
            //关闭---按范围大小关闭
            producer.close();
            session.close();
            connection.close();
        }catch (JMSException e){
            e.printStackTrace();
        }

    }
    private String initWareOrder(String orderId){
        //通过orderId查询orderInfo
        OrderInfo orderInfo = getOrderInfo(orderId);
        //将orderInfo转化为map
        Map map  = initWareOrder(orderInfo);
        return JSON.toJSONString(map);
    }

    private Map initWareOrder(OrderInfo orderInfo){
        //创建map集合
        HashMap<String, Object> map = new HashMap<>();
        map.put("orderId",orderInfo.getId());
        map.put("consignee", orderInfo.getConsignee());
        map.put("consigneeTel",orderInfo.getConsigneeTel());
        map.put("orderComment",orderInfo.getOrderComment());
        map.put("orderBody","测试数据");
        map.put("deliveryAddress",orderInfo.getDeliveryAddress());
        map.put("paymentWay","2");
        //map.put("wareId",orderInfo.getWareId());    拆单使用
        //再声明一个集合存储orderDetailList对象
        ArrayList<Map> newOrderDetailList = new ArrayList<>();
        //操作订单明细
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            HashMap<String,Object> detailMap = new HashMap<>();
            detailMap.put("skuId",orderDetail.getSkuId());
            detailMap.put("skuName",orderDetail.getSkuName());
            detailMap.put("skuNum",orderDetail.getSkuNum());
            newOrderDetailList.add(detailMap);      //list里的一个map对象
            //{。。。}，{。。。}，{key：value，key：value}
        }
        map.put("details",newOrderDetailList);
        return  map;


    }

}
