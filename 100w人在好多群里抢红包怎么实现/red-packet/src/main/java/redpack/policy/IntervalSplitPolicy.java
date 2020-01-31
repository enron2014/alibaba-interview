package redpack.policy;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import redpack.model.RedPacket;
import redpack.util.LuaExecutor;

@Component("intervalSplitPolicy")
public class IntervalSplitPolicy implements RedPacketPolicy {

	private static final String KEY = "IS_RED_PACK";

	private SecureRandom secureRandom = new SecureRandom();

	private DefaultRedisScript<Integer> initScript;

	private DefaultRedisScript<Integer> grabScript;

	private LuaExecutor luaExecutor;

	public IntervalSplitPolicy(LuaExecutor luaExecutor) {
		this.luaExecutor = luaExecutor;
		this.initScript = getInitScript();
		this.grabScript = getGrabScript();
	}

	private DefaultRedisScript<Integer> getInitScript() {
		DefaultRedisScript<Integer> script = new DefaultRedisScript<>();
		script.setLocation(new ClassPathResource("lua/intervalSplit/initial.lua"));
		return script;
	}

	private DefaultRedisScript<Integer> getGrabScript() {
		DefaultRedisScript<Integer> script = new DefaultRedisScript<>();
		script.setLocation(new ClassPathResource("lua/intervalSplit/grab.lua"));
		return script;
	}

	@Override
	public void init(RedPacket redPacket) {
		List<Integer> cutPoints = getCutPoints(redPacket.getAmount(), redPacket.getCount());
		cutPoints.sort(Integer::compareTo);
		List<Integer> amounts = getSplitAmounts(redPacket.getAmount(), cutPoints);
		List<String> args = Lists.newArrayList(String.valueOf(redPacket.getCount()));
		amounts.stream().map(String::valueOf).forEach(args::add);
		luaExecutor.eval(initScript.getScriptAsString(),
			Lists.newArrayList(getKey(redPacket.getId())),
			args);
	}

	private List<Integer> getCutPoints(Integer amount, Integer count) {
		List<Integer> cutPoints = new ArrayList<>();
		for (int i = 0; i < count - 1; i++) {
			int point = secureRandom.nextInt(amount - 1) + 1;
			while(cutPoints.contains(point)) {
				point = secureRandom.nextInt(amount - 1) + 1;
			}
			cutPoints.add(point);
		}
		return cutPoints;
	}

	private List<Integer> getSplitAmounts(Integer amount, List<Integer> cutPoints) {
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
		return result;
	}

	private String getKey(Integer id) {
		return KEY + "_" + id;
	}

	@Override
	public Integer grab(Integer id) {
		Long value = luaExecutor.eval(grabScript.getScriptAsString(),
			Lists.newArrayList(getKey(id)),
			Lists.newArrayList("1"));
		return value.intValue();
	}
}
