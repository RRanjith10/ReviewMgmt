package com.mindtree.review.management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;

@Configuration
public class HazelCastCacheConfig {

	@Bean
	public Config hazelCastConfig() {
		return new Config().setInstanceName("reviewbyid").setInstanceName("reviewbycustemail")
				.setInstanceName("reviewbyrstid")
				.addMapConfig(new MapConfig().setName("reviewmanagement")
						.setMaxSizeConfig(new MaxSizeConfig(200, MaxSizeConfig.MaxSizePolicy.FREE_HEAP_SIZE))
						.setEvictionPolicy(EvictionPolicy.LRU).setTimeToLiveSeconds(100));
	}
}
