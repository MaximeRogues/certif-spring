package com.maximerogues.cashcard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/cashcards")
public class CashCardController {

    private final CashCardRepository cashCardRepository;

    private CashCardController(CashCardRepository cashCardRepository) {
        this.cashCardRepository = cashCardRepository;
    }

    @GetMapping("/{requestedId}")
    private ResponseEntity<CashCard> findById(@PathVariable Long requestedId, Principal principal) {
        CashCard cashCard = findCashCard(requestedId, principal);
        if (cashCard != null) {
            return ResponseEntity.ok(cashCard);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    private ResponseEntity<List<CashCard>> findAll(Pageable pageable, Principal principal) {
        Page<CashCard> page = cashCardRepository.findByOwner(
                principal.getName(),
                PageRequest.of(
                        pageable.getPageNumber(), // 0 par défaut
                        pageable.getPageSize(),   // 20 par défaut (fonctionnement de Spring Web)
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
                ));
        return ResponseEntity.ok(page.getContent());
    }

    @PostMapping
    private ResponseEntity<CashCard> create(@RequestBody CashCard newCashCard, Principal principal) {
        // on ajoute le Owner à la Cashcard
        CashCard newCashCardWithOwner = new CashCard(null, newCashCard.amount(), principal.getName());

        CashCard savedCashCard = cashCardRepository.save(newCashCardWithOwner);
        return ResponseEntity.created(URI.create("/cashcards/" + savedCashCard.id())).build();
    }

    @PutMapping("/{requestedId}")
    private ResponseEntity<Void> putCashCard(@PathVariable Long requestedId, @RequestBody CashCard cashCardData, Principal principal) {
        // récupération de la cashcard en base
        CashCard existingCashCard = findCashCard(requestedId, principal);
        if(existingCashCard != null) {
            // sauvegarde de la cashcard avec ses nouvelles infos
            CashCard updatedCashCard = new CashCard(existingCashCard.id(), cashCardData.amount(), principal.getName());
            cashCardRepository.save(updatedCashCard);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{requestedId}")
    private ResponseEntity<Void> deleteCashCard(@PathVariable Long requestedId, Principal principal) {
        // récupération de la cashcard en base
        if(cashCardRepository.existsByIdAndOwner(requestedId, principal.getName())) {
            // suppression de la cashcard
            cashCardRepository.deleteById(requestedId);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

    private CashCard findCashCard(Long requestedId, Principal principal) {
        return cashCardRepository.findByIdAndOwner(requestedId, principal.getName());
    }
}
