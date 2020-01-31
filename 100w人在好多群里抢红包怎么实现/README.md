##场景分析
该场景是一个高并发抢有限资源的场景，多个人同时抢红包，要保证线程安全，先到先得。
考虑使用redis的单线程特性，用LUA脚本实现抢红包算法。

如果并发量太高，还需要考虑一下限流（令牌桶算法）。

##常见红包算法（整理）
###1、剩余金额随机算法
剩余红包金额M，剩余人数N，那么：每次抢到金额=随机[1，M-N]

优点是实现简单，容易理解，缺点是一开始抢到的多，越往后越少。

LUA算法脚本：
```
local exist = redis.pcall("EXISTS", KEYS[1])

if (exist == 0) then
    return 0
end

local red_packet_info = redis.pcall("HMGET", KEYS[1], "remain_amount", "remain_count")
local remain_amount = tonumber(red_packet_info[1])
local remain_count = tonumber(red_packet_info[2])

if (remain_count == 1) then
    redis.pcall("DEL", KEYS[1]);
    return remain_amount;
end

local time = redis.pcall("time")
math.randomseed(tostring(time[1]):reverse():sub(1, 7))
local amount = math.random(remain_amount - remain_count)
redis.pcall("HMSET", KEYS[1], "remain_amount", tostring(remain_amount - amount));
redis.pcall("HMSET", KEYS[1], "remain_count", tostring(remain_count - 1));
return amount;
```

###2、二倍均值法
剩余红包金额M，剩余人数N，那么：每次抢到金额=随机[1，M/N*2-1]

剩余红包金额M，剩余人数N，每个人抢到的数学期望是E = M / N，

假设范围是[1, x],
每个数字随机到的概率都是1/x，那么数学期望

```
E = 1/x * 1 + 1/x * 2 + ... + 1/x * x
  = ((1 + x) x) / 2x
  = (1 + x) / 2
M / N = (1 + x) / 2
  x = M / N * 2 - 1
```

优点是保证了每次随机金额的平均值是公平的，缺点是每次最多只能抢到平均值的2倍。

LUA算法脚本：

```
local exist = redis.pcall("EXISTS", KEYS[1])

if (exist == 0) then
    return 0
end

local red_packet_info = redis.pcall("HMGET", KEYS[1], "remain_amount", "remain_count")
local remain_amount = tonumber(red_packet_info[1])
local remain_count = tonumber(red_packet_info[2])

if (remain_count == 1) then
    redis.pcall("DEL", KEYS[1]);
    return remain_amount;
end

local time = redis.pcall("time")
math.randomseed(tostring(time[1]):reverse():sub(1, 7))
local amount = math.random(remain_amount / remain_count * 2 - 1)
redis.pcall("HMSET", KEYS[1], "remain_amount", tostring(remain_amount - amount));
redis.pcall("HMSET", KEYS[1], "remain_count", tostring(remain_count - 1));
return amount;

```

###3、线段分割法
把红包总金额想象成一条很长的线段，而每个人抢到的金额，则是这条主线段所拆分出的若干子线段。
当N个人一起抢红包的时候，就需要确定N-1个切割点。

优点是分配平均，缺点是需要先分配好，容易出现重复切割点，且占用内存空间多。

源码
```
List<Integer> cutPoints = new ArrayList<>();
for (int i = 0; i < count - 1; i++) {
    int point = secureRandom.nextInt(amount - 1) + 1;
    while(cutPoints.contains(point)) {
        point = secureRandom.nextInt(amount - 1) + 1;
    }
    cutPoints.add(point);
}
cutPoints.sort(Integer::compareTo);
List<Integer> result = new ArrayList<>();
for (int i = 0; i < cutPoints.size(); i++) {
    if (i == 0) {
        result.add(cutPoints.get(i));
    }
    else {
        result.add(cutPoints.get(i) - cutPoints.get(i - 1));
    }
}
result.add(amount - cutPoints.get(cutPoints.size() - 1));
```

##附录
LUA随机数算法
```
local time = redis.pcall("time")
math.randomseed(tostring(time[1]):reverse():sub(1, 7))
math.random(1, x)
```

https://blog.csdn.net/goodai007/article/details/59579515

源码见 red-packet
