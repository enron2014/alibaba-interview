package redpack.policy;

import java.util.Collections;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import redpack.model.RedPacket;
import redpack.util.LuaExecutor;

@Component("averageRandomPolicy")
public class AverageRandomPolicy implements RedPacketPolicy {

	private static final String KEY = "AVR_RED_PACK";

	private DefaultRedisScript<Integer> initScript;

	private DefaultRedisScript<Integer> grabScript;

	private LuaExecutor luaExecutor;

	public AverageRandomPolicy(LuaExecutor luaExecutor) {
		this.luaExecutor = luaExecutor;
		this.initScript = getInitScript();
		this.grabScript = getGrabScript();
	}

	private DefaultRedisScript<Integer> getInitScript() {
		DefaultRedisScript<Integer> script = new DefaultRedisScript<>();
		script.setLocation(new ClassPathResource("lua/averageRandom/initial.lua"));
		return script;
	}

	private DefaultRedisScript<Integer> getGrabScript() {
		DefaultRedisScript<Integer> script = new DefaultRedisScript<>();
		script.setLocation(new ClassPathResource("lua/averageRandom/grab.lua"));
		return script;
	}

	@Override
	public void init(RedPacket redPacket) {
		luaExecutor.eval(initScript.getScriptAsString(),
			Collections.singletonList(getRedPacketKey(redPacket.getId())),
			Lists.newArrayList(String.valueOf(redPacket.getAmount()), String.valueOf(redPacket.getCount())));
	}

	private String getRedPacketKey(Integer id) {
		return KEY + "_" + id;
	}

	@Override
	public Integer grab(Integer id) {
		Long value = luaExecutor.eval(grabScript.getScriptAsString(),
			Collections.singletonList(getRedPacketKey(id)),
			Lists.newArrayList("1"));
		return value.intValue();
	}

}
