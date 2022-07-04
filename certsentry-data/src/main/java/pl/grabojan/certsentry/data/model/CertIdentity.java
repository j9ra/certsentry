package pl.grabojan.certsentry.data.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
@Entity
@Table(name="CERT_IDENTITY")
public class CertIdentity implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="CERT_IDENTITY_SEQ")
	@Column(name="ID")
	private Long id;
	
	@Column(name="SERIAL_NUMBER", nullable = false)
	private String serialNumber;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="NOT_BEFORE", nullable = false)
	private Date notBefore;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="NOT_AFTER", nullable = false)
	private Date notAfter;
	
	@Column(name="ISSUER", nullable = false, length = 500)
	private String issuer;
	
	@Column(name="SUBJECT", nullable = false, length = 500)
	private String subject;
	
	@Column(name="PUBLIC_KEY_HASH", nullable = false)
	private String publicKeyHash;
	
	@Column(name="SIGNATURE_ALGO", nullable = false)
	private String signatureAlgo;
	
	@JsonIgnore
	//@Lob
	@Column(name="VALUE", nullable = false, columnDefinition = "bytea")
	@Basic() 
	private byte[] value;
	
	@JsonBackReference
	@ManyToOne
	@JoinColumn(name="SERVICE_ID")
	private Service service;

}
