package redpack.factory;

import java.util.Random;

import org.springframework.stereotype.Component;

import redpack.model.RedPacket;

@Component
public class RedPacketFactory {

	public RedPacketFactory() {
	}

	public RedPacket createRedPacket(Integer amount, Integer count) {
		return RedPacket.builder().id(nextId()).amount(amount).count(count).build();
	}

	private Integer nextId() {
		return new Random().nextInt(100000);
	}

}
