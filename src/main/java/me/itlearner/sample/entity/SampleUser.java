package me.itlearner.sample.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 测试用户
 *
 * @author gaochen
 * @date 2019/9/20
 */
@Data
@Entity
@Table
public class SampleUser {

    @Id
    @GeneratedValue(generator = "user-id-generator")
    @GenericGenerator(name = "user-id-generator",
            parameters = @Parameter(name = "idGroup", value = "user"),
            strategy = "me.itlearner.sample.idgenerator.TinyIdGenerator")
    private Long id;

    private String username;

    private String password;

}
