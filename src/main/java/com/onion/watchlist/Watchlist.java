package com.onion.watchlist;

import com.onion.product.product.Product;
import com.onion.user.User;
import lombok.*;

import javax.persistence.*;


@Entity
@Getter @Setter
@EqualsAndHashCode(of = "id")
@AllArgsConstructor @NoArgsConstructor
@Table(name = "watchlist_items")
public class Watchlist {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne
	@JoinColumn(name = "product_id")	
	private Product product;


	public Watchlist(User newUser, Product savedProduct) {
		this.user=newUser;
		this.product=savedProduct;
	}
}
