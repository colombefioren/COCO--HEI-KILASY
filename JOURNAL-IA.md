# Journal IA — Kilasy

- **Outil** : Claude Code (modèle Claude Sonnet 5.5), dans un terminal
- **Langue** : échanges en anglais et en français, traduits et résumés ici

## Prise en main de Kilasy

### Échange 1 · 19:18 — Ports déjà occupés

**Demande**  
`docker compose up -d` ne fonctionne pas.

**Proposition**

- Le port 5432 est pris par mon PostgreSQL local et le port 8080 par une autre application.
- Arrêter ces services, ou changer les ports de Kilasy.

**Décision**  
Arrêter les services.

**Pourquoi**  
Les fichiers de configuration du projet restent identiques à ceux du dépôt.

**Vérification**  
Erreur Docker `address already in use`, puis liste des ports libérés (assistant).

### Échange 2 · 19:24 — Connexion refusée à la base

**Demande**  
`mvn spring-boot:run` échoue avec `Connection to localhost:5432 refused`.

**Proposition**

- Le conteneur PostgreSQL tourne sans port publié.
- Le recréer avec `docker compose down` puis `docker compose up -d`.

**Décision**  
Recréer les conteneurs.

**Pourquoi**  
Les données sont des données de démonstration, recréées au démarrage.

**Vérification**  
PostgreSQL accepte les connexions sur le port 5432 (assistant).

### Échange 3 · 19:42 — Exploration de l’API

**Demande**  
Terminer la prise en main.

**Proposition**

- Appeler les points d’entrée en lecture et consulter Mailpit.

**Décision**  
Gardé.

**Pourquoi**  
Connaître le comportement actuel avant de le modifier.

**Vérification**  
Tous les `GET` répondent 200. Renoter une soumission renvoie un email (assistant).

## Changement C1 : pénalité de retard

### Échange 4 · 19:44 — Calcul de la pénalité

**Demande**  
Réaliser C1 : −10 % par jour de retard entamé, plafond −50 %, même note dans la correction, le relevé et l’export.

**Proposition**

- La note était calculée à trois endroits avec des arrondis différents (13,7 dans le CSV, 13,8 dans le relevé).
- Une seule méthode `gradeOutOf20` dans `GradeReport`, appelée partout.
- Appliquer la pénalité, puis arrondir une seule fois au dixième.
- Arrondir les points saisis à deux décimales, comme la colonne de la base.
- Ajouter la règle au README.

**Décision**  
Gardé.

**Pourquoi**  
Un seul calcul empêche les trois affichages de diverger, sans nouvelle classe ni réorganisation.

**Vérification**  
12 soumissions comparées entre le détail, le relevé et le CSV : aucun écart. Pour 16/20 brut : 14,4, 11,2 et 8 (assistant).

### Échange 5 · 19:48 — Message de commit

**Demande**  
Remplacer le message `C1` par `feat: proceed with C1`.

**Proposition**

- Changement fait, avec un rappel : la consigne demande le message `C1`.

**Décision**  
Retour au message `C1`.

**Pourquoi**  
La consigne impose ce message.

**Vérification**  
Historique Git du fork (assistant).

### Échange 6 · 19:50 — Deux points non précisés par la consigne

**Demande**  
Vérifier qu’il ne manque rien à C1.

**Proposition**

- Le retard se compte à partir de la fin du jour de l’échéance.
- La pénalité s’applique aussi aux notes saisies avant le changement.

**Décision**  
Code inchangé. Le second point est à confirmer avec le client.

**Pourquoi**  
Le premier suit la règle existante des soumissions en retard. Le client n’a prévu aucune exception pour le second.

**Vérification**  
Les notes saisies avant le changement (Bob, Clara, David) s’affichent pénalisées (assistant).

### Échange 7 · 19:55 — Règle dans le README

**Demande**  
Faut-il vraiment modifier le README ?

**Proposition**

- La ligne n’était pas demandée.
- La garder, car le README liste les règles métier.

**Décision**  
Ligne gardée, après avoir d’abord voulu ne pas modifier le README.

**Pourquoi**  
Sans elle, la liste des règles métier serait incomplète.

**Vérification**  
Le diff du README contient une seule ligne ajoutée (assistant).

## Bilan C1

**Code écrit par l’assistant**

- `GradeReport.java` : calcul commun de la note avec pénalité
- `SubmissionController.java` : correction, email et détail utilisent ce calcul
- `CsvExport.java` : l’export utilise ce calcul
- `README.md` : règle de pénalité

**Effet visible** : certaines notes de l’export changent d’un dixième (13,7 devient 13,8), car l’arrondi est commun.
