首先，我们需要建立一个用户表（user）和一个钱包表（wallet），
用户表存储用户的基本信息，如id，姓名，手机号等，
钱包表存储用户的钱包信息，如id，用户id，余额，状态等。
两个表通过用户id关联。建表语句如下：

-- 创建用户表
CREATE TABLE user (
  id INT PRIMARY KEY, -- 用户id
  name VARCHAR(20) NOT NULL, -- 用户姓名
  phone VARCHAR(11) NOT NULL -- 用户手机号
);

-- 创建钱包表
CREATE TABLE wallet (
  id INT PRIMARY KEY, -- 钱包id
  user_id INT NOT NULL, -- 用户id
  balance DECIMAL(10,2) NOT NULL DEFAULT 0.00, -- 钱包余额
  status VARCHAR(10) NOT NULL DEFAULT 'normal', -- 钱包状态，正常（normal）或冻结（frozen）
  version INT NOT NULL, -- 版本 控制并发
  FOREIGN KEY (user_id) REFERENCES user (id) -- 外键约束，关联用户表
);

-- 创建钱包明细表
CREATE TABLE wallet_detail (
  id INT PRIMARY KEY, -- 明细id
  wallet_id INT NOT NULL, -- 钱包id
  amount DECIMAL(10,2) NOT NULL, -- 金额，正数为收入，负数为支出
  lastAmount DECIMAL(10,2) NOT NULL, -- 上次金额
  currAmount DECIMAL(10,2) NOT NULL, -- 变更后金额
  type VARCHAR(10) NOT NULL, -- 交易类型，如充值（recharge），提现（withdraw），消费（consume），退款（refund）等
  version INT NOT NULL, -- 版本 控制并发
  remark VARCHAR(100) NOT NULL, -- 交易备注，如购买商品，申请退款等
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 交易时间
  FOREIGN KEY (wallet_id) REFERENCES wallet (id) -- 外键约束，关联钱包表
);

