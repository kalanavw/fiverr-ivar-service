package com.ivar.ivarservice.mode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
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
public class Result
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false, updatable = false)
	private long id;

	@Column(name = "SYMBOL")
	private String Symbol;

	@Column(name = "Y1")
	private String Y1;

	@Column(name = "Y3")
	private String Y3;

	@Column(name = "Y5")
	private String Y5;

	@Column(name = "Y10")
	private String Y10;

	@Column(name = "NORMY1")
	private String NormY1;

	@Column(name = "NORMY3")
	private String NormY3;

	@Column(name = "NORMY5")
	private String NormY5;

	@Column(name = "NORMY10")
	private String NormY10;

	@Column(name = "PERFY1")
	private String PerfY1;

	@Column(name = "PERFY3")
	private String PerfY3;

	@Column(name = "PERFY5")
	private String PerfY5;

	@Column(name = "PERFY10")
	private String PerfY10;

	@Column(name = "CREATED_DATE", nullable = false, updatable = false)
	@CreatedDate
	private LocalDateTime createdDate = LocalDateTime.now();

	public Result( String symbol, String y1, String y3, String y5, String y10, String normY1, String normY3, String normY5, String normY10, String perfY1, String perfY3, String perfY5, String perfY10 )
	{
		Symbol = symbol;
		Y1 = y1;
		Y3 = y3;
		Y5 = y5;
		Y10 = y10;
		NormY1 = normY1;
		NormY3 = normY3;
		NormY5 = normY5;
		NormY10 = normY10;
		PerfY1 = perfY1;
		PerfY3 = perfY3;
		PerfY5 = perfY5;
		PerfY10 = perfY10;
	}
}
