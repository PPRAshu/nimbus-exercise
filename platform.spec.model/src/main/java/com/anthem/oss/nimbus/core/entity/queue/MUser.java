package com.anthem.oss.nimbus.core.entity.queue;

import java.util.HashSet;
import java.util.Set;

import com.anthem.oss.nimbus.core.domain.definition.AssociatedEntity;
import com.anthem.oss.nimbus.core.domain.definition.ConfigNature.Ignore;
import com.anthem.oss.nimbus.core.domain.definition.Domain;
import com.anthem.oss.nimbus.core.domain.definition.Domain.ListenerType;
import com.anthem.oss.nimbus.core.domain.definition.Repo;
import com.anthem.oss.nimbus.core.entity.AbstractEntity.IdString;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Rakesh Patel
 *
 */
@Domain(value="muser", includeListeners={ListenerType.persistence})
@Repo
@Getter @Setter
public class MUser extends IdString {
	
	@Ignore
	private static final long serialVersionUID = 1L;
	
	private String id;
	
	private String name;
	
	private String code;
	
	@AssociatedEntity(clazz=MUserGroup.class)
	private Set<String> userGroups;
	
	@AssociatedEntity(clazz=Queue.class)
	private Set<String> queues;
	
	public void addUserGroups(MUserGroup ug) {
		if(getUserGroups() == null) {
			setUserGroups(new HashSet<>());
		}
		getUserGroups().add(ug.getName());
	}
	
	public void addQueues(Queue q) {
		if(getQueues() == null) {
			setQueues(new HashSet<>());
		}
		getQueues().add(q.getName());
	}

}
