package com.ecommerce.beta.entity;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

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
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OnlineOrderRef extends BaseEntity {
	
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Type(type = "org.hibernate.type.UUIDCharType")
    private UUID uuid;

    private boolean status;
    private String comment;
    private String razorPayOrderId;

    //on success
    private String paymentId;
    private String signature;

    //on failure
    private String errorCode;
    private String errorDesc;
    private String errorSource;
    private String errorStep;
    private String errorReason;
    private String errorPaymentId;


    @JoinColumn(name = "order_id")
    @OneToOne
    private OrderHistory orderHistory;

}
