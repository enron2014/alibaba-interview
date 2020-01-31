package redpack.policy;

import redpack.model.RedPacket;

public interface RedPacketPolicy {

	void init(RedPacket redPacket);

	Integer grab(Integer id);

}
