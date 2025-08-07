package io.bank.mortgage.domain.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("user_roles")
public class UserRole {
    @Column("user_id")
    private Long userId;

    @Column("role_id")
    private Integer roleId;
}