package com.onion.domain.product;

import com.onion.domain.Location;
import com.onion.domain.User;
import com.onion.domain.Watchlist;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.*;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name = "products")
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	protected Integer id;

	@Column(length = 255, nullable = false)
	private String name;

	@Column(length = 40, nullable = false, name = "short_description")
	private String shortDescription;

	@Column(length = 1000, nullable = false, name = "full_description")
	private String fullDescription;
	
	@Column(name = "created_time")
	private Date createdTime;

	private float price;

	@Column(name = "main_image", nullable = false)
	private String mainImage;
		
	@ManyToOne
	@JoinColumn(name = "location_id")
	private Location location;

	@ManyToOne( fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "seller_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private User seller;

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Watchlist> watchlist=new HashSet<>();

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<ProductImage> images = new HashSet<>();

    public Product(Integer id) {
		this.id = id;
    }


    public void addExtraImage(String imageName) {
		this.images.add(new ProductImage(imageName, this));
	}


	@Transient
	public String getMainImagePath() {
		if (id == null || mainImage == null) return "/images/image-thumbnail.png";

		return "/product-images/" + this.id + "/" + this.mainImage;
	}

	public boolean containsImageName(String imageName) {
		Iterator<ProductImage> iterator = images.iterator();
		
		while (iterator.hasNext()) {
			ProductImage image = iterator.next();
			if (image.getName().equals(imageName)) {
				return true;
			}
		}
		
		return false;
	}
	

	@Transient
	public String getURI() {
		return "/p/" + this.id + "/";
	}

}
