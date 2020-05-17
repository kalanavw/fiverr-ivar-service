package com.ivar.ivarservice.mode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Copyright (c) 2018. scicom.com.my - All Rights Reserved
 * Created by kalana.w on 5/14/2020.
 */
@Data
@Entity
@DynamicUpdate
@Table(name = "RESULTS")
@NoArgsConstructor
public class Result
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false, updatable = false)
	private long id;

	@Column(name = "SYMBOL")
	private String symbol;

	@Column(name = "Y1")
	private String y1;

	@Column(name = "Y3")
	private String y3;

	@Column(name = "Y5")
	private String y5;

	@Column(name = "Y10")
	private String y10;

	@Column(name = "NORMY1")
	private String normY1;

	@Column(name = "NORMY3")
	private String normY3;

	@Column(name = "NORMY5")
	private String normY5;

	@Column(name = "NORMY10")
	private String normY10;

	@Column(name = "PERFY1")
	private String perfY1;

	@Column(name = "PERFY3")
	private String perfY3;

	@Column(name = "PERFY5")
	private String perfY5;

	@Column(name = "PERFY10")
	private String perfY10;

	@Column(name = "CREATED_DATE", nullable = false, updatable = false)
	@CreatedDate
	private LocalDateTime createdDate = LocalDateTime.now();

	public Result( String symbol, String y1, String y3, String y5, String y10, String normY1, String normY3, String normY5, String normY10, String perfY1, String perfY3, String perfY5, String perfY10 )
	{
		this.symbol = symbol;
		this.y1 = y1;
		this.y3 = y3;
		this.y5 = y5;
		this.y10 = y10;
		this.normY1 = normY1;
		this.normY3 = normY3;
		this.normY5 = normY5;
		this.normY10 = normY10;
		this.perfY1 = perfY1;
		this.perfY3 = perfY3;
		this.perfY5 = perfY5;
		this.perfY10 = perfY10;
	}
}
