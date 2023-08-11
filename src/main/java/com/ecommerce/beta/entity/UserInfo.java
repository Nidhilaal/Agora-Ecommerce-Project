package com.ecommerce.beta.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserInfo {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Type(type = "org.hibernate.type.UUIDCharType")
	private UUID uuid;
	
	@Column(unique=true)
	@NotNull(message ="username is empty")
	private String username;
	
	@NotNull(message="firstname is empty")
	private String firstName;
	
	@NotNull(message="lastname is empty")
	private String lastName;
	
	@NotNull(message="password is empty")
	private String password;
	
	@Column(unique = true)
	@Email(message="invalid mail")
	private String email;
	
	@Column(unique = true)
	private String phone;
	
	@OneToMany(mappedBy = "userInfo")
    @ToString.Exclude
    private List<Address> savedAddresses = new ArrayList<>();
	
	private boolean enabled=true;
	
	private boolean verified=true;
	
	@ManyToOne(cascade = CascadeType.MERGE)
	@JoinColumn(name = "role_id")
	private Role role;
	
	@ManyToOne
    @JoinColumn(name="coupon_id")
    private Coupon coupon;

    @OneToMany(mappedBy = "userInfo")
    @ToString.Exclude
    private List<Cart> cartItems = new ArrayList<>();

    @OneToMany(mappedBy = "userInfo")
    @ToString.Exclude
    private List<OrderHistory> orderHistories = new ArrayList<>();

}
