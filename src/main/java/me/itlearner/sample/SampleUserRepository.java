package me.itlearner.sample;

import me.itlearner.sample.entity.SampleUser;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * TODO
 *
 * @author gaochen
 * @date 2019/9/20
 */
public interface SampleUserRepository extends JpaRepository<SampleUser, Long> {
}
