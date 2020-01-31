local result=1
redis.pcall("HMSET", KEYS[1],
    "remain_amount", ARGV[1],
    "remain_count", ARGV[2])
return result
