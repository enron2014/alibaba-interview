local exist = redis.pcall("EXISTS", KEYS[1])

if (exist == 0) then
    return 0
end

local amount = tonumber(redis.pcall("LPOP", KEYS[1]))
local length = tonumber(redis.pcall("LLEN", KEYS[1]))
if (length == 0) then
    redis.pcall("DEL", KEYS[1])
end
return amount
