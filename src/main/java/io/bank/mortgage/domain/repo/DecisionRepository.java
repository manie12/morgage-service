package io.bank.mortgage.domain.repo;

public interface DecisionRepository extends JpaRepository<Decision, UUID> {
  List<Decision> findTop1ByApplicationIdOrderByDecidedAtDesc(UUID appId);
}