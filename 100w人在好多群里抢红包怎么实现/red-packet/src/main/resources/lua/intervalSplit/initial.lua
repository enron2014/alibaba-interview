local result=1
local number = tonumber(ARGV[1])
for i = 1, number do
    redis.pcall("RPUSH", KEYS[1], ARGV[i + 1])
end
return result
