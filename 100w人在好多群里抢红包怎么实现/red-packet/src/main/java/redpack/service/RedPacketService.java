package redpack.service;

public interface RedPacketService {

	Integer newRedPacket(Integer amount, Integer count);

	Integer grabRedPacket(Integer id);

}
