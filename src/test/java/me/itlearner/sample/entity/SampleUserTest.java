package me.itlearner.sample.entity;

import lombok.extern.slf4j.Slf4j;
import me.itlearner.sample.SampleUserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * TODO
 *
 * @author gaochen
 * @date 2019/9/20
 */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class SampleUserTest {

    @Autowired
    private SampleUserRepository sampleUserRepository;

    @Test
    public void test() throws InterruptedException {
        for (int i = 0; i < 1000; i++) {
            new Thread(() -> {
                SampleUser user1 = new SampleUser();
                user1.setUsername("test");
                user1.setPassword("test");

                SampleUser sampleUser = sampleUserRepository.save(user1);

                log.info("user id : {}", sampleUser.getId());
            }).start();
        }

        Thread.sleep(10000);
    }

}