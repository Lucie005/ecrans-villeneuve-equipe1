# Diagrammes

Un fichier par diagramme, **numéroté dans l'ordre du DAT**. L'outil est libre ; deux bons choix :

- **draw.io** : chaque fichier `.drawio` est déjà créé, avec sa question en titre. Ouvrez-le (app.diagrams.net → *Ouvrir un fichier existant*, ou le plugin IntelliJ « Diagrams.net Integration ») et dessinez dessous.
- **PlantUML** : écrivez le diagramme en texte dans un fichier `.puml` du même nom (plugin IntelliJ « PlantUML Integration »), et supprimez le `.drawio` correspondant.

Pour chaque diagramme, le dossier contient à la fin **deux fichiers** :

- sa source (`.drawio`, `.puml`…), pour pouvoir le modifier ;
- son export **PNG**, du même nom, que vous insérez dans le DAT.

| Fichier | Section du DAT | Question à laquelle il répond |
|---|---|---|
| `1-contexte.drawio` | 3 | Avec qui la billetterie échange-t-elle ? |
| `2-context-map.drawio` | 4 | Comment les contextes collaborent-ils ? |
| `3-composants-vente.drawio` | 6 | Comment la vente de places est-elle branchée sur le monde extérieur ? |
| `4-sequence-reserver-payer.drawio` | 7 | Comment des places sont-elles réservées puis payées… ou libérées ? |
| `5-modele-vente.drawio` | 8 | Quels agrégats protègent les règles de la vente ? |

Une séquence facultative peut s'ajouter : `6-sequence-<scenario>` (source + PNG).

## Règles de lisibilité

- Le titre en forme de **question** reste en haut du diagramme.
- Une **légende** ou des zones colorées nommées (adapters primaires, application, domaine, adapters secondaires, externes).
- Les noms sont **ceux du glossaire** et du code.
- Interfaces UML : cercle = fournie (port primaire), demi-cercle = requise (port secondaire).
- Séquences : flèche pleine = appel synchrone, flèche ouverte = message asynchrone, pointillés = retour.
