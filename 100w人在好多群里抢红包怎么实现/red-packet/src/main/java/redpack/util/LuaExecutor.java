package redpack.util;

import java.util.List;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

@Component
public class LuaExecutor {

	private RedisTemplate<String, String> redisTemplate;

	public LuaExecutor(RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@SuppressWarnings("unchecked")
	public <T> T eval(final String script, final List<String> keys, final List<String> args) {
		return redisTemplate.execute((RedisCallback<T>) connection -> {
			Object nativeConnection = connection.getNativeConnection();
			// 集群
			if (nativeConnection instanceof JedisCluster) {
				return (T) ((JedisCluster) nativeConnection).eval(script, keys, args);
			}

			// 单点
			else if (nativeConnection instanceof Jedis) {
				return (T) ((Jedis) nativeConnection).eval(script, keys, args);
			}
			return null;
		});
	}

}
