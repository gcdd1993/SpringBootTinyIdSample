package me.itlearner.sample.idgenerator;

import com.xiaoju.uemc.tinyid.client.utils.TinyId;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.util.Properties;

/**
 * TinyId 分布式ID生成器
 *
 * @author gaochen
 * @date 2019/9/20
 */
public class TinyIdGenerator implements IdentifierGenerator, Configurable {

    private String idGroup;

    @Override
    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
        idGroup = params.getProperty("idGroup");
    }

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        return TinyId.nextId(idGroup);
    }
}
