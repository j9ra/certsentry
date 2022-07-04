package pl.grabojan.certsentry.data.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@Entity
@Table(name="SEC_USER")
public class SecUser implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="USERNAME", nullable = false)
	private String username;

	
	@Column(name="PASSWORD", nullable = false)
	private String password;
	
	@Column(name="ENABLED", nullable = false)
	private Boolean enabled;
	
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name="SEC_ROLE", joinColumns = @JoinColumn(name="USERNAME"))
	@Column(name="AUTHORITY")
	private Set<String> authorities = new HashSet<>();
	
	@JsonBackReference
	@OneToOne(orphanRemoval = true, cascade = { CascadeType.ALL })
	@JoinColumn(name="APPLICATION_ID", nullable = true)
	private Application application;
		
	@JsonIgnore
	public String getPassword() {
	    return password;
	}

	@JsonProperty
	public void setPassword(String password) {
	    this.password = password;
	}
}
