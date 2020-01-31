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
