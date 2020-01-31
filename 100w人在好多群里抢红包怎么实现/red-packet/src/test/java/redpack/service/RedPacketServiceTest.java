package redpack.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import redpack.StartApplication;
import redpack.factory.RedPacketFactory;
import redpack.policy.AllRandomPolicy;
import redpack.policy.AverageRandomPolicy;
import redpack.policy.IntervalSplitPolicy;
import redpack.policy.RedPacketPolicy;
import redpack.repository.RedPacketRepository;
import redpack.service.impl.RedPacketServiceImpl;

@SpringBootTest(classes = StartApplication.class)
@RunWith(SpringRunner.class)
public class RedPacketServiceTest {

	@Autowired
	private RedPacketFactory redPacketFactory;

	@Autowired
	private RedPacketRepository redPacketRepository;

	@Autowired
	private AllRandomPolicy allRandomPolicy;

	@Autowired
	private AverageRandomPolicy averageRandomPolicy;

	@Autowired
	private IntervalSplitPolicy intervalSplitPolicy;

	@Test
	public void testAllRandomRedPacket() throws InterruptedException {
		// 200块红包，分成20个，100个人抢
		testGrabRedPacket(allRandomPolicy, 20000, 20, 100);
		Thread.sleep(1000);
	}

	@Test
	public void testAverageRandomRedPacket() throws InterruptedException {
        // 200块红包，分成20个，100个人抢
		testGrabRedPacket(averageRandomPolicy, 20000, 20, 100);
		Thread.sleep(1000);
	}

	@Test
	public void testIntervalSplitRedPacket() throws InterruptedException {
        // 200块红包，分成20个，100个人抢
		testGrabRedPacket(intervalSplitPolicy, 20000, 20, 100);
		Thread.sleep(1000);
	}

	private void testGrabRedPacket(RedPacketPolicy policy, int amount, int count, int threads) {
		RedPacketService redPacketService = new RedPacketServiceImpl(policy, redPacketFactory, redPacketRepository);
		Integer id = redPacketService.newRedPacket(amount, count);
		System.out.println("红包id:" + id);
		grab(redPacketService, id, threads);
	}

	private void grab(RedPacketService redPacketService, Integer id, int threads) {
		ExecutorService executorService = Executors.newFixedThreadPool(50);
		for (int i = 0; i < threads; i++) {
			// 采用线程池控制并发量
			executorService.submit(() -> {
				Integer amount = redPacketService.grabRedPacket(id);
				if (amount > 0) {
					System.out.println(Thread.currentThread().getName() + " 抢到" + amount / 100.0);
				}
				else {
					System.out.println(Thread.currentThread().getName() + " 抢光了");
				}
			});
		}
	}
}
