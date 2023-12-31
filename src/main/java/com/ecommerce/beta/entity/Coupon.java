package com.ecommerce.beta.entity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.springframework.format.annotation.DateTimeFormat;

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
public class Coupon extends BaseEntity {
	
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Type(type = "org.hibernate.type.UUIDCharType")
    private UUID uuid;
    
    private String name;
    
    @Column(unique = true, nullable = false)
    private String code;
    
    private int offPercentage;
    private int maxOff;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    
    private int count;
    private int couponType; // 1-product, 2-category, 3-brand, 4-all, 5-user,
    private UUID applicableFor; //uuid of applicable items

    private boolean enabled = true;

    @OneToMany(mappedBy = "coupon")
    @ToString.Exclude
    private List<UserInfo> users;

    @OneToMany(mappedBy = "coupon")
    private List<OrderHistory> orderHistoryList;

    public boolean isExpired(){
        return (this.count == 0 || this.expiryDate.isBefore(LocalDate.now()));
    }

}
