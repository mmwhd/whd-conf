package com.whd.conf.core.spring;

import com.whd.conf.core.ConfClient;
import com.whd.conf.core.exception.ConfException;
import com.whd.conf.core.factory.ConfBaseFactory;
import com.whd.conf.core.listener.impl.BeanRefreshConfListener;
import com.whd.conf.core.util.FieldReflectionUtil;
import com.whd.conf.core.annotation.Conf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.*;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Conf Factory
 *
 * @author hayden 2015-9-12 19:42:49
 */
public class ConfFactory extends InstantiationAwareBeanPostProcessorAdapter
		implements InitializingBean, DisposableBean, BeanNameAware, BeanFactoryAware {

	private static Logger logger = LoggerFactory.getLogger(ConfFactory.class);


	// ---------------------- env config ----------------------

	private String envprop;		// like "conf.properties" or "file:/data/webapps/conf.properties", include the following env config

	private String zkaddress;
	private String zkdigest;
	private String env;
	private String mirrorfile;

	public void setEnvprop(String envprop) {
		this.envprop = envprop;
	}

	public void setZkaddress(String zkaddress) {
		this.zkaddress = zkaddress;
	}

    public void setZkdigest(String zkdigest) {
        this.zkdigest = zkdigest;
    }

	public void setEnv(String env) {
		this.env = env;
	}

    public void setMirrorfile(String mirrorfile) {
        this.mirrorfile = mirrorfile;
    }

    // ---------------------- init/destroy ----------------------

	@Override
	public void afterPropertiesSet() {

		if (envprop!=null && envprop.trim().length()>0) {
			ConfBaseFactory.init(envprop);
		} else {
			ConfBaseFactory.init(zkaddress, zkdigest, env, mirrorfile);
		}

	}

	@Override
	public void destroy() {
		ConfBaseFactory.destroy();
	}


	// ---------------------- post process / xml、annotation ----------------------

	@Override
	public boolean postProcessAfterInstantiation(final Object bean, final String beanName) throws BeansException {


		// 1、Annotation('@Conf')：resolves conf + watch
		if (!beanName.equals(this.beanName)) {

			ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {
				@Override
				public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
					if (field.isAnnotationPresent(Conf.class)) {
						String propertyName = field.getName();
						Conf conf = field.getAnnotation(Conf.class);

						String confKey = conf.value();
						String confValue = ConfClient.get(confKey, conf.defaultValue());


						// resolves placeholders
						BeanRefreshConfListener.BeanField beanField = new BeanRefreshConfListener.BeanField(beanName, propertyName);
						refreshBeanField(beanField, confValue, bean);

						// watch
						if (conf.callback()) {
							BeanRefreshConfListener.addBeanField(confKey, beanField);
						}

					}
				}
			});
		}

		return super.postProcessAfterInstantiation(bean, beanName);
	}

	@Override
	public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {

		// 2、XML('$Conf{...}')：resolves placeholders + watch
		if (!beanName.equals(this.beanName)) {

			PropertyValue[] pvArray = pvs.getPropertyValues();
			for (PropertyValue pv : pvArray) {
				if (pv.getValue() instanceof TypedStringValue) {
					String propertyName = pv.getName();
					String typeStringVal = ((TypedStringValue) pv.getValue()).getValue();
					if (xmlKeyValid(typeStringVal)) {

						// object + property
						String confKey = xmlKeyParse(typeStringVal);
						String confValue = ConfClient.get(confKey, "");

						// resolves placeholders
						BeanRefreshConfListener.BeanField beanField = new BeanRefreshConfListener.BeanField(beanName, propertyName);
						//refreshBeanField(beanField, confValue, bean);

						Class propClass = String.class;
						for (PropertyDescriptor item: pds) {
							if (beanField.getProperty().equals(item.getName())) {
								propClass = item.getPropertyType();
							}
						}
						Object valueObj = FieldReflectionUtil.parseValue(propClass, confValue);
						pv.setConvertedValue(valueObj);

						// watch
						BeanRefreshConfListener.addBeanField(confKey, beanField);

					}
				}
			}

		}

		return super.postProcessPropertyValues(pvs, pds, bean, beanName);
	}

	// ---------------------- refresh bean with conf  ----------------------

	/**
	 * refresh bean with conf (fieldNames)
	 */
	public static void refreshBeanField(final BeanRefreshConfListener.BeanField beanField, final String value, Object bean){
		if (bean == null) {
			bean = ConfFactory.beanFactory.getBean(beanField.getBeanName());		// getBean 会导致Bean提前初始化，风险较大；
		}
		if (bean == null) {
			return;
		}

		BeanWrapper beanWrapper = new BeanWrapperImpl(bean);

		// property descriptor
		PropertyDescriptor propertyDescriptor = null;
		PropertyDescriptor[] propertyDescriptors = beanWrapper.getPropertyDescriptors();
		if (propertyDescriptors!=null && propertyDescriptors.length>0) {
			for (PropertyDescriptor item: propertyDescriptors) {
				if (beanField.getProperty().equals(item.getName())) {
					propertyDescriptor = item;
				}
			}
		}

		// refresh field: set or field
		if (propertyDescriptor!=null && propertyDescriptor.getWriteMethod() != null) {
			beanWrapper.setPropertyValue(beanField.getProperty(), value);	// support mult data types
			logger.info(">>>>>>>>>>> conf, refreshBeanField[set] success, {}#{}:{}",
					beanField.getBeanName(), beanField.getProperty(), value);
		} else {

			final Object finalBean = bean;
			ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {
				@Override
				public void doWith(Field fieldItem) throws IllegalArgumentException, IllegalAccessException {
					if (beanField.getProperty().equals(fieldItem.getName())) {
						try {
							Object valueObj = FieldReflectionUtil.parseValue(fieldItem.getType(), value);

							fieldItem.setAccessible(true);
							fieldItem.set(finalBean, valueObj);		// support mult data types

							logger.info(">>>>>>>>>>> conf, refreshBeanField[field] success, {}#{}:{}",
									beanField.getBeanName(), beanField.getProperty(), value);
						} catch (IllegalAccessException e) {
							throw new ConfException(e);
						}
					}
				}
			});

			/*Field[] beanFields = bean.getClass().getDeclaredFields();
			if (beanFields!=null && beanFields.length>0) {
				for (Field fieldItem: beanFields) {
					if (beanField.getProperty().equals(fieldItem.getName())) {
						try {
							Object valueObj = FieldReflectionUtil.parseValue(fieldItem.getType(), value);

							fieldItem.setAccessible(true);
							fieldItem.set(bean, valueObj);		// support mult data types

							logger.info(">>>>>>>>>>> conf, refreshBeanField[field] success, {}#{}:{}",
									beanField.getBeanName(), beanField.getProperty(), value);
						} catch (IllegalAccessException e) {
							throw new ConfException(e);
						}
					}
				}
			}*/
		}

	}


	// ---------------------- util ----------------------

	/**
	 * register beanDefinition If Not Exists
	 *
	 * @param registry
	 * @param beanClass
	 * @param beanName
	 * @return
	 */
	public static boolean registerBeanDefinitionIfNotExists(BeanDefinitionRegistry registry, Class<?> beanClass, String beanName) {

		// default bean name
		if (beanName == null) {
			beanName = beanClass.getName();
		}

		if (registry.containsBeanDefinition(beanName)) {	// avoid beanName repeat
			return false;
		}

		String[] beanNameArr = registry.getBeanDefinitionNames();
		for (String beanNameItem : beanNameArr) {
			BeanDefinition beanDefinition = registry.getBeanDefinition(beanNameItem);
			if (Objects.equals(beanDefinition.getBeanClassName(), beanClass.getName())) {	// avoid className repeat
				return false;
			}
		}

		BeanDefinition annotationProcessor = BeanDefinitionBuilder.genericBeanDefinition(beanClass).getBeanDefinition();
		registry.registerBeanDefinition(beanName, annotationProcessor);
		return true;
	}


	private static final String placeholderPrefix = "$Conf{";
	private static final String placeholderSuffix = "}";

	/**
	 * valid xml
	 *
	 * @param originKey
	 * @return
	 */
	private static boolean xmlKeyValid(String originKey){
		boolean start = originKey.startsWith(placeholderPrefix);
		boolean end = originKey.endsWith(placeholderSuffix);
		if (start && end) {
			return true;
		}
		return false;
	}

	/**
	 * parse xml
	 *
	 * @param originKey
	 * @return
	 */
	private static String xmlKeyParse(String originKey){
		if (xmlKeyValid(originKey)) {
			// replace by conf
			String key = originKey.substring(placeholderPrefix.length(), originKey.length() - placeholderSuffix.length());
			return key;
		}
		return null;
	}


	// ---------------------- other ----------------------

	private String beanName;
	@Override
	public void setBeanName(String name) {
		this.beanName = name;
	}

	private static BeanFactory beanFactory;
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

}
