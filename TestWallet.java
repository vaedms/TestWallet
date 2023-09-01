package com.lt.trade.module.service.domain.response.supplier;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.ibatis.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class TestWallet {


    @Autowired
    private UserWalletMapper userWalletMapper;
    @Autowired
    private UserWalletDetailMapper userWalletDetailMapper;
    /**
     * 1 查询用户钱包余额
     * 这个接口的功能是根据用户id查询用户的钱包余额，并返回结果
     */
    public BigDecimal queryWallet(Long userId){
        if (userId == null){
            return BigDecimal.ZERO;
        }
        UserWallet userWallet = userWalletMapper.queryUserBalance(userId);
        if (userWallet == null){
            return BigDecimal.ZERO;
        }
        return userWallet.getBalance();
    }

    /**
     * 2 用户消费100元的接口
     * 这个接口的功能是根据用户id扣除用户钱包中的100元，并记录消费明细
     */
    public static final String OPRTYPE_CONSUME = "consume";

    @Transactional(rollbackFor = Exception.class)
    public void consume(Long userId, BigDecimal amount){
        if (userId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new RuntimeException("参数异常!");
        }
        //查询钱包
        UserWallet userWallet = userWalletMapper.queryUserBalance(userId);
        BigDecimal balance = userWallet.getBalance();
        if (userWallet == null){
            throw new RuntimeException("请检查钱包是否冻结!");
        }
        //插入消费明细
        int updateCount = userWalletMapper.cosume(userId, amount, userWallet.version);
        if (updateCount <= 0){
            throw new RuntimeException("请检查钱包余额是否足够!");
        }
        //记录钱包余额明细
        UserWallet newUserWallet = userWalletMapper.queryUserBalance(userId);
        UserWalletDetail userWalletDetail = new UserWalletDetail();
        userWalletDetail.setWallet_id(userWallet.getId());
        userWalletDetail.setAmount(amount);
        userWalletDetail.setLastAmount(balance);
        userWalletDetail.setCurrAmount(newUserWallet.getBalance());
        userWalletDetail.setType(OPRTYPE_CONSUME);
        userWalletDetail.setVersion(newUserWallet.getVersion());
        userWalletDetail.setRemark(null);
        userWalletDetail.setCreate_time(new Date());
        userWalletDetailMapper.insert(userWalletDetail);
    }

    /**
     * 3 用户退款20元接口
     * 这这个接口的功能是根据用户id增加用户钱包中的20元，并记录退款明细
     */
    public static final String OPRTYPE_REFUND = "refund";

    @Transactional(rollbackFor = Exception.class)
    public void refund(Long userId, BigDecimal amount){
        if (userId == null || amount == null || amount.compareTo(BigDecimal.ZERO) >= 0){
            throw new RuntimeException("参数异常!");
        }
        //查询钱包
        UserWallet userWallet = userWalletMapper.queryUserBalance(userId);
        BigDecimal balance = userWallet.getBalance();
        if (userWallet == null){
            throw new RuntimeException("请检查钱包是否冻结!");
        }
        //插入消费明细
        int updateCount = userWalletMapper.refund(userId, amount, userWallet.version);
        if (updateCount <= 0){
            throw new RuntimeException("钱包金额发生变更, 请重试!");
        }
        //记录钱包余额明细
        UserWallet newUserWallet = userWalletMapper.queryUserBalance(userId);
        UserWalletDetail userWalletDetail = new UserWalletDetail();
        userWalletDetail.setWallet_id(userWallet.getId());
        userWalletDetail.setAmount(amount);
        userWalletDetail.setLastAmount(balance);
        userWalletDetail.setCurrAmount(newUserWallet.getBalance());
        userWalletDetail.setType(OPRTYPE_REFUND);
        userWalletDetail.setVersion(newUserWallet.getVersion());
        userWalletDetail.setRemark(null);
        userWalletDetail.setCreate_time(new Date());
        userWalletDetailMapper.insert(userWalletDetail);
    }

    /**
     * 4 查询用户钱包金额变动明细的接口
     * 这个接口的功能是根据用户id查询用户的钱包金额变动明细，并返回结果
     */

    @Transactional(rollbackFor = Exception.class)
    public List<UserWalletDetail> queryWalletDetail(Long userId){
        if (userId == null){
            return null;
        }
        return userWalletDetailMapper.queryWalletDetail(userId);
    }


/**
 * 用户钱包mapper
 */
@Mapper
interface UserWalletMapper{

    @Select("<script> " +
            "SELECT id, user_id, balance, status, version FROM wallet WHERE user_id = #{user_id} and status = 'normal'" +
            "</script>")
    UserWallet queryUserBalance(@Param("user_id") Long user_id);

    @Update("<script> " +
            "UPDATE wallet SET balance = balance - #{amount} , verison = (#{version} + 1) WHERE user_id = #{user_id} and (balance - #{amount})) >= 0 and version = #{version}" +
            "</script>")
    int cosume(@Param("user_id") Long user_id, @Param("amount") BigDecimal amount, @Param("version") Integer version);

    @Update("<script> " +
            "UPDATE wallet SET balance = balance - #{amount} , verison = (#{version} + 1) WHERE user_id = #{user_id} and version = #{version}" +
            "</script>")
    int refund(@Param("user_id") Long user_id, @Param("amount") BigDecimal amount, @Param("version") Integer version);

}

    /**
     * 用户钱包明细mapper
     */
@Mapper
interface UserWalletDetailMapper{
        @Insert("INSERT INTO wallet_detail (wallet_id, amount, lastAmount, currAmount type, remark) VALUES (?, -100/+20, 1000, 900,  'consume', '购买商品');")
        int insert(@Param("userWalletDetail") UserWalletDetail userWalletDetail);

        @Select("<script> " +
                "SELECT amount, lastAmount, currAmount, type, remark, create_time FROM wallet_detail WHERE wallet_id = ? ORDER BY version DESC;" +
                "</script>")
        List<UserWalletDetail> queryWalletDetail(@Param("user_id") Long userId);

    }



    /**
* 用户
*/
@Data
class User{

    @ApiModelProperty("用户ID 自增主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户姓名")
    private String name;

    @ApiModelProperty("用户手机号")
    private String phone;

}

/**
* 用户钱包
*/
@Data
class UserWallet{

    @ApiModelProperty("自增主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户ID")
    private Long user_id;

    @ApiModelProperty("钱包余额")
    private BigDecimal balance;

    @ApiModelProperty("钱包状态，正常（normal）或冻结（frozen）")
    private String status;

    @ApiModelProperty("版本 控制并发")
    private Integer version;

}

/**
 * 用户钱包明细
 */
@Data
class UserWalletDetail{


    @ApiModelProperty("自增主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("钱包id")
    private Long wallet_id;

    @ApiModelProperty("金额，正数为收入，负数为支出")
    private BigDecimal amount;

    @ApiModelProperty("上次金额")
    private BigDecimal lastAmount;

    @ApiModelProperty("变更后金额")
    private BigDecimal currAmount;

    @ApiModelProperty("交易类型，如充值（recharge），提现（withdraw），消费（consume），退款（refund）")
    private String type;

    @ApiModelProperty("版本 控制并发")
    private Integer version;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("创建时间")
    private Date create_time;

}



}



