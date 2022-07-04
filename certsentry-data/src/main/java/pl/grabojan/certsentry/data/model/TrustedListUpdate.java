package pl.grabojan.certsentry.data.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.Data;

@Data
@Entity
@Table(name="TRUSTED_LIST_UPDATE")
public class TrustedListUpdate implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="TRUSTED_LIST_UPDATE_SEQ")
	@Column(name="ID")
	private Long id;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="TIMESTAMP", nullable = false)
	private Date timestamp;
	
	@Enumerated(EnumType.STRING)
	@Column(name="STATUS", nullable = false)
	private UpdateStatus status;
	
	@Column(name="ARCH_LOCAL_URI")
	private String archLocalUri;
	
	@Column(name="INFO")
	private String info;
	
	@Column(name="ERROR_CODE")
	private Integer errorCode;
	
	@JsonBackReference
	@ManyToOne
	@JoinColumn(name="TRUSTED_LIST_ID")
	private TrustedList trustedList;
	
}
