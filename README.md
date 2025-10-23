# NeverForget

Application Android de gestion des tâches d'entretien peu fréquentes avec système de rappels intelligents et récurrence automatique.

## 📋 Description

NeverForget vous aide à ne jamais oublier les tâches d'entretien importantes mais peu fréquentes comme :
- Changer les détecteurs de fumée
- Nettoyer la ventilation
- Vérifier la pression des pneus
- Et bien d'autres...

## ✨ Fonctionnalités

### Version 1.0 (MVP)
- ✅ 6 tâches pré-configurées au premier lancement
- ✅ CRUD complet des tâches (Créer, Lire, Modifier, Supprimer)
- ✅ Validation avec récurrence automatique
- ✅ Historique persistant de toutes les validations
- ✅ Notifications le jour J (11h par défaut)
- ✅ Interface moderne avec Jetpack Compose et Material Design 3
- ✅ Export/Import JSON pour sauvegarder vos données
- ✅ Gestion des catégories personnalisées
- ✅ Tri automatique par urgence

### Prévues pour V2
- 🔄 Personnalisation de l'heure des notifications
- 🔄 Rappels automatiques après X jours
- 🔄 Fonction Snooze configurable
- 🔄 Gestion intelligente des doublons à l'import
- 🔄 Statistiques d'utilisation
- 🔄 Thème sombre/clair

## 🛠️ Technologies

- **Langage** : Kotlin
- **Interface** : Jetpack Compose + Material Design 3
- **Architecture** : MVVM (Model-View-ViewModel)
- **Base de données** : Room
- **Notifications** : WorkManager
- **Injection de dépendances** : Hilt
- **Sérialisation** : Kotlinx Serialization

## 📱 Captures d'écran

*À venir lors de la Phase 4*

## 🚀 Installation

### Prérequis
- Android 7.0 (API 24) ou supérieur
- Android Studio Arctic Fox ou plus récent

### Compilation
1. Clonez le repository
```bash
git clone https://github.com/kapoue/neverforget.git
cd neverforget
```

2. Ouvrez le projet dans Android Studio

3. Synchronisez les dépendances Gradle

4. Compilez et installez sur votre appareil

## 📦 Structure du projet

```
NeverForget/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/neverforget/
│   │   │   │   ├── data/          # Room, Repository
│   │   │   │   ├── ui/            # Screens, ViewModels
│   │   │   │   ├── workers/       # WorkManager
│   │   │   │   ├── utils/         # Helpers, Extensions
│   │   │   │   └── MainActivity.kt
│   │   │   ├── res/               # Layouts, Drawables, Strings
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   └── build.gradle.kts
├── gradle/
├── README.md
└── build.gradle.kts
```

## 🎯 Utilisation

### Première utilisation
1. Lancez l'application
2. 6 tâches par défaut sont automatiquement créées
3. Consultez la liste triée par urgence
4. Validez les tâches terminées d'un simple clic

### Gestion des tâches
- **Ajouter** : Bouton ⊕ en bas à droite
- **Modifier** : Cliquez sur une tâche puis "Modifier"
- **Supprimer** : Depuis le détail de la tâche
- **Valider** : Bouton ✓ sur chaque tâche

### Paramètres
Accédez aux paramètres via le menu hamburger (≡) :
- **Notifications** : Configurez l'heure des rappels
- **Catégories** : Ajoutez/supprimez des catégories personnalisées

### Sauvegarde
- **Export** : Sauvegardez vos données en JSON
- **Import** : Restaurez depuis un fichier JSON

## 🔔 Notifications

L'application envoie des notifications :
- Le jour de l'échéance à 11h (configurable)
- Avec actions rapides : Valider, Reporter, Voir

## 📄 Licence

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

## 👨‍💻 Développement

### Plan de développement
Le projet suit un plan de développement en 10 phases :
1. ✅ Structure et Configuration
2. 🔄 Base de Données et Modèles (Room)
3. ⏳ Repository et ViewModels (MVVM)
4. ⏳ Interface Utilisateur (Jetpack Compose)
5. ⏳ Logique Métier et Récurrence
6. ⏳ Système de Notifications (WorkManager)
7. ⏳ Export/Import JSON
8. ⏳ Gestion des Catégories
9. ⏳ Tests et Finalisation
10. ⏳ Documentation et Release

### Contribution
Les contributions sont les bienvenues ! N'hésitez pas à :
- Signaler des bugs
- Proposer des améliorations
- Soumettre des pull requests

## 📞 Contact

- **Développeur** : kapoue
- **GitHub** : [github.com/kapoue/neverforget](https://github.com/kapoue/neverforget)

---

**Version actuelle** : 1.0.0 (en développement)  
**Dernière mise à jour** : 23 octobre 2025