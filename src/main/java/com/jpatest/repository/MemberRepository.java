package com.jpatest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jpatest.domain.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
}
