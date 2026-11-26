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

## Changement C2 : notification par SMS

### Échange 8 · 20:39 — Où placer le code SMS

**Demande**  
Réaliser C2 en me demandant mon avis sur les choix.

**Proposition**

- Deux petits fichiers : une interface et une simulation.
- Ou tout placer dans `KilasyApplication.java`.

**Décision**  
Deux nouveaux fichiers.

**Pourquoi**  
La notation ne dépend que de l’interface : changer de fournisseur ne la modifie pas.

**Vérification**  
`SubmissionController` ne mentionne pas la classe de simulation (assistant).

### Échange 9 · 20:40 — Échec du SMS

**Demande**  
Que voit l’enseignant si le SMS échoue ?

**Proposition**

- La même réponse qu’avant, avec l’échec écrit dans les logs.
- Ou un champ `smsSent`.

**Décision**  
Même réponse qu’avant.

**Pourquoi**  
Le client n’a pas demandé de changer la réponse de l’API.

**Vérification**  
Panne simulée par une exception : réponse 200, note enregistrée, échec dans les logs (assistant).

### Échange 10 · 20:44 — Email et README

**Demande**  
Relire les limites du changement.

**Proposition**

- Le SMS part après l’email ; si l’email échoue, le SMS ne part pas.
- Mentionner le SMS dans le README.

**Décision**  
Email inchangé, README complété.

**Pourquoi**  
C2 ne demande pas de modifier l’email, et le README doit rester exact.

**Vérification**  
Envoi normal : email puis ligne `[SMS]` dans les logs (assistant).

### Échange 11 · 20:48 — Revue de sécurité

**Demande**  
Faut-il traiter la remarque de la revue de sécurité ?

**Proposition**

- La simulation écrit le numéro et la note dans les logs.
- Le noter sans changer le code.

**Décision**  
Noté, code inchangé.

**Pourquoi**  
Le client demande ce message et les numéros sont fictifs. La simulation ne doit pas aller en production telle quelle.

**Vérification**  
La ligne des logs suit le format de la consigne (assistant).

## Bilan C2

**Code écrit par l’assistant**

- `SmsSender.java` (nouveau) : interface d’envoi
- `LogSmsSender.java` (nouveau) : simulation dans les logs
- `SubmissionController.java` : envoi après l’email, dans un `try/catch`
- `README.md` : mention du SMS

**Limite** : l’envoi est synchrone, un fournisseur lent ralentirait la notation.

## Changement C3 : exercices minutés

### Échange 12 · 20:50 — Deuxième appel à `start`

**Demande**  
Réaliser C3 en me demandant mon avis sur les choix.

**Proposition**

- Garder le premier départ.
- Ou répondre 409.

**Décision**  
Garder le premier départ.

**Pourquoi**  
Le décompte ne doit pas pouvoir être remis à zéro.

**Vérification**  
Deux appels à `start` donnent la même heure de départ (assistant).

### Échange 13 · 20:50 — Soumission sans `start`

**Demande**  
Que faire d’une soumission sans appel à `start` ?

**Proposition**

- La refuser avec un 422.
- Ou l’accepter.

**Décision**  
La refuser, avec un message qui indique d’ouvrir l’exercice.

**Pourquoi**  
Sinon la limite de temps se contourne facilement.

**Vérification**  
Réponse 422 avec le message attendu (assistant).

### Échange 14 · 20:50 — Temps utilisé et README

**Demande**  
Sous quelle forme afficher le temps utilisé ?

**Proposition**

- Minutes entières arrondies au supérieur.
- Ou minutes et secondes.
- Documenter le changement dans le README.

**Décision**  
Minutes entières (`timeUsedMinutes`), README complété.

**Pourquoi**  
C’est la même unité que la durée fixée par l’enseignant.

**Vérification**  
Un départ reculé de 12 minutes donne 13 dans le relevé (assistant).

### Échange 15 · 20:55 — Énoncé visible avant `start`

**Demande**  
Une revue de sécurité signale que la description d’un exercice minuté est visible avant `start`.

**Proposition**

- La masquer dans la liste et le détail, et la renvoyer avec `start`.
- Ou seulement noter la limite.

**Décision**  
Corriger et noter.

**Pourquoi**  
Un étudiant pouvait lire l’énoncé et se préparer avant le décompte.

**Vérification**  
Description absente avant `start`, renvoyée par `start` (assistant).

## Bilan C3

**Code écrit par l’assistant**

- `schema.sql` : durée, échéance facultative, table `exercise_start`
- `Exercise.java`, `ExerciseController.java` : durée et point d’entrée `start`
- `SubmissionController.java` : refus 422
- `GradeReport.java`, `CsvExport.java` : échéance absente et temps utilisé
- `README.md` : nouvelles règles

**Vérification des critères** (assistant, base de test temporaire)

- Durée en plus ou à la place de l’échéance : OK
- Soumission après la durée refusée (422) : OK
- Exercices sans durée inchangés : OK
- Pénalité de C1 sur un exercice minuté avec échéance (16 devient 14,4) : OK
- Temps utilisé dans le relevé : OK

**Limites**

- Sans authentification, n’importe qui peut appeler `start` pour un autre étudiant, et l’enseignant ne relit plus la description par l’API.
- Les scripts SQL ne modifient pas une table existante : il faut recréer la base.

**Constat** : une seule nouvelle règle a obligé à modifier presque tous les fichiers.
