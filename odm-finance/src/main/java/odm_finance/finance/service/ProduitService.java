package odm_finance.finance.service;


import odm_finance.finance.model.Produit;
import odm_finance.finance.repository.ProduitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProduitService {
    @Autowired
    private ProduitRepository produitRepository;


    // Créer un produit
    public Produit createProduit(Produit produit) {
        return produitRepository.save(produit);
    }

    // Récupérer tous les produits
    public List<Produit> getAllProduits() {
        return produitRepository.findAll();
    }

    // Récupérer un produit par ID
    public Optional<Produit> getProduitById(Integer id) {
        return produitRepository.findById(id);
    }

    // Mettre à jour un produit
    public Produit updateProduit(Integer id, Produit produitDetails) {
        Optional<Produit> produit = produitRepository.findById(id);
        if (produit.isPresent()) {
            Produit existingProduit = produit.get();
            existingProduit.setNom(produitDetails.getNom());
            existingProduit.setPrix(produitDetails.getPrix());
            existingProduit.setDescription(produitDetails.getDescription());
            existingProduit.setQuantite(produitDetails.getQuantite());
            existingProduit.setImageUrl(produitDetails.getImageUrl());
            return produitRepository.save(existingProduit);
        } else {
            return null; // Produit non trouvé
        }
    }

    // Supprimer un produit
    public void deleteProduit(Integer id) {
        produitRepository.deleteById(id);
    }
}
