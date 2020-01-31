package redpack.service.impl;

import redpack.factory.RedPacketFactory;
import redpack.model.RedPacket;
import redpack.policy.RedPacketPolicy;
import redpack.repository.RedPacketRepository;
import redpack.service.RedPacketService;

public class RedPacketServiceImpl implements RedPacketService {

	private RedPacketPolicy policy;

	private RedPacketFactory redPacketFactory;

	private RedPacketRepository redPacketRepository;

	public RedPacketServiceImpl(RedPacketPolicy policy, RedPacketFactory redPacketFactory,
			RedPacketRepository redPacketRepository) {
		this.policy = policy;
		this.redPacketFactory = redPacketFactory;
		this.redPacketRepository = redPacketRepository;
	}

	@Override
	public Integer newRedPacket(Integer amount, Integer count) {
		RedPacket redPacket = redPacketFactory.createRedPacket(amount, count);
		policy.init(redPacket);
		redPacketRepository.save(redPacket);
		return redPacket.getId();
	}

	@Override
	public Integer grabRedPacket(Integer id) {
		return policy.grab(id);
	}

}
