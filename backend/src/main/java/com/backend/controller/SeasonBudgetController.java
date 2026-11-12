
package com.backend.controller;

import com.backend.dto.budget.BudgetAllocationRequest;
import com.backend.dto.budget.BudgetCategoryResponse;
import com.backend.dto.budget.SeasonBudgetRequest;
import com.backend.dto.budget.SeasonBudgetResponse;
import com.backend.entity.User;
import com.backend.service.SeasonBudgetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/farms/{farmId}")
@RequiredArgsConstructor
public class SeasonBudgetController {

    private final SeasonBudgetService seasonBudgetService;

    @PostMapping("/budgets")
    public ResponseEntity<SeasonBudgetResponse> createBudget(
            @PathVariable Long farmId,
            @Valid @RequestBody SeasonBudgetRequest request,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(seasonBudgetService.createBudget(
                        farmId, request, user.getId()));
    }

    @PostMapping("/budgets/{budgetId}/allocations")
    public ResponseEntity<BudgetCategoryResponse> addAllocation(
            @PathVariable Long farmId,
            @PathVariable Long budgetId,
            @Valid @RequestBody BudgetAllocationRequest request,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(seasonBudgetService.addAllocation(
                        farmId, budgetId, request, user.getId()));
    }

    @GetMapping("/budgets/{budgetId}")
    public ResponseEntity<SeasonBudgetResponse> getBudget(
            @PathVariable Long farmId,
            @PathVariable Long budgetId,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);

        return ResponseEntity.ok(
                seasonBudgetService.getBudget(
                        farmId, budgetId, user.getId()));
    }

    @GetMapping("/crop-cycles/{cropCycleId}/budget")
    public ResponseEntity<SeasonBudgetResponse> getBudgetByCropCycle(
            @PathVariable Long farmId,
            @PathVariable Long cropCycleId,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);

        return ResponseEntity.ok(
                seasonBudgetService.getBudgetByCropCycle(
                        farmId, cropCycleId, user.getId()));
    }

    private User getAuthenticatedUser(HttpServletRequest request) {

        Object value = request.getAttribute("authenticatedUser");

        if (!(value instanceof User user)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required"
            );
        }

        return user;
    }
}
