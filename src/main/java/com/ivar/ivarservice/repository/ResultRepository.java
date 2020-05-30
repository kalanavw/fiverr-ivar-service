package com.ivar.ivarservice.repository;

import com.ivar.ivarservice.model.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Copyright (c) 2018. scicom.com.my - All Rights Reserved
 * Created by kalana.w on 5/14/2020.
 */
@Repository
public interface ResultRepository extends JpaRepository<Result, Long>
{
}
