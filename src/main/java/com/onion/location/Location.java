package com.onion.location;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "locations")
public class Location  {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	protected Integer id;

	@Column(length = 128, nullable = false, unique = true)
	private String name;
	private boolean enabled;

	@Column(name = "all_parent_ids", length = 256, nullable = true)
	private String allParentIDs;

	@OneToOne( fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private Location parent;

	@OneToMany(mappedBy = "parent")
	@OrderBy("name asc")
	private Set<Location> children = new HashSet<>();

	public Location() {
	}

	public Location(Integer id) {
		this.id = id;
	}

	public static Location copyIdAndName(Location location) {
		Location copyLocation = new Location();
		copyLocation.setId(location.getId());
		copyLocation.setName(location.getName());

		return copyLocation;
	}

	public static Location copyIdAndName(Integer id, String name, boolean hasChildren) {
		Location copyLocation = new Location();
		copyLocation.setId(id);
		copyLocation.setName(name);
		copyLocation.setHasChildren(true);

		return copyLocation;
	}

	// 카테고리 insert 시 '--' 삽입을 위해 copy
	public static Location copyFull(Location location) {
		Location copyLocation = new Location();
		copyLocation.setId(location.getId());
		copyLocation.setName(location.getName());
		copyLocation.setEnabled(location.isEnabled());
		copyLocation.setHasChildren(location.getChildren().size() > 0);

		return copyLocation;
	}

	public static Location copyFull(Location location, String name) {
		Location copyLocation = Location.copyFull(location);
		copyLocation.setName(name);

		return copyLocation;
	}

	public Location(String name) {
		this.name = name;
	}

	public Location(String name, Location parent) {
		this(name);
		this.parent = parent;
	}

	public Location(Integer id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}



	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Location getParent() {
		return parent;
	}

	public void setParent(Location parent) {
		this.parent = parent;
	}

	public Set<Location> getChildren() {
		return children;
	}

	public void setChildren(Set<Location> children) {
		this.children = children;
	}
	

	public boolean isHasChildren() {
		return hasChildren;
	}

	public void setHasChildren(boolean hasChildren) {
		this.hasChildren = hasChildren;
	}

	@Transient
	private boolean hasChildren;

	@Override
	public String toString() {
		return this.name;
	}

	public String getAllParentIDs() {
		return allParentIDs;
	}

	public void setAllParentIDs(String allParentIDs) {
		this.allParentIDs = allParentIDs;
	}

	
}
