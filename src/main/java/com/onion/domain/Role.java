package com.onion.domain;

import lombok.*;

import javax.persistence.*;
import java.util.Collection;
import java.util.Set;

@Entity
@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
@Table(name = "roles")
public class Role {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	protected Integer id;

	@Column(length = 40, nullable = false, unique = true)
	private String name;

	public Role(String name) {
		this.name = name;
	}


}
