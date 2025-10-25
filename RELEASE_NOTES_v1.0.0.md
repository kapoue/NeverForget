# 🚀 NeverForget v1.0.0 - Release Notes

## 🎯 Vue d'ensemble

**NeverForget** est une application Android complète de gestion des tâches d'entretien peu fréquentes (maison, véhicules, etc.) avec système de rappels intelligents et récurrence automatique.

## ✨ Fonctionnalités principales

### 📋 Gestion des tâches
- **CRUD complet** : Créer, lire, modifier, supprimer des tâches
- **Récurrence automatique** : Calcul intelligent de la prochaine échéance
- **6 tâches pré-configurées** au premier lancement :
  - Détecteurs de fumée (6 mois)
  - Ventilation salle de bain (3 mois)
  - Filtres aspirateur (2 mois)
  - Pression pneus voiture (1 mois)
  - Niveau lave-glace (6 mois)
  - Pression pneus scooter (1 mois)

### 🔔 Système de notifications avancé
- **Notifications le jour J** avec actions directes
- **Rappels automatiques** après délai configurable (par défaut 3 jours)
- **Fonction Snooze** avec options multiples :
  - 1 heure
  - 3 heures
  - 1 jour
  - 3 jours
- **Actions dans les notifications** : Valider ou Reporter directement

### 💾 Sauvegarde et partage
- **Export JSON** avec nom de fichier horodaté
- **Partage cloud** (Google Drive, OneDrive, etc.)
- **Import simplifié** qui écrase toutes les données existantes
- **Format JSON structuré** avec versioning

### 🎨 Interface moderne
- **Material Design 3** avec thème dynamique
- **Jetpack Compose** pour une UI fluide
- **Tri par urgence** : tâches les plus proches en premier
- **Indicateurs visuels** :
  - 🟢 "Dans X jours"
  - 🟡 "À faire aujourd'hui"
  - 🔴 "En retard de X jours"

### 📂 Gestion des catégories
- **Catégories par défaut** : Maison, Voiture, Scooter, Autre
- **Catégories personnalisées** : Ajout/suppression
- **Icônes dédiées** pour chaque catégorie
- **Migration automatique** lors de suppression de catégorie

### 📊 Historique complet
- **Suivi de toutes les validations** avec dates
- **Affichage chronologique** (plus récent en haut)
- **Persistance des données** même après modification des tâches

## 🏗️ Architecture technique

### Stack principal
- **Kotlin** (dernière version stable)
- **Jetpack Compose** pour l'interface utilisateur
- **Material Design 3** pour le design system
- **Android SDK** : API minimale 24, cible API 34+

### Architecture
- **MVVM** (Model-View-ViewModel) avec Architecture Components
- **Repository Pattern** pour l'accès aux données
- **Hilt** pour l'injection de dépendances
- **Coroutines & Flow** pour la programmation réactive

### Base de données
- **Room ORM** avec SQLite
- **Migrations automatiques** pour les mises à jour
- **Relations complexes** avec clés étrangères
- **Type converters** pour LocalDate

### Notifications
- **WorkManager** pour les tâches en arrière-plan
- **BroadcastReceivers** pour les actions de notifications
- **NotificationManager** avec canaux dédiés
- **Planification exacte** avec AlarmManager

### Traitement d'annotations
- **KSP** (Kotlin Symbol Processing) - Compatible Java 21
- **Migration depuis KAPT** pour de meilleures performances
- **Support complet** des annotations Room et Hilt

## 📱 Compatibilité

- **Android 7.0** (API 24) minimum
- **Android 14** (API 34) cible
- **Java 21** compatible
- **Kotlin 1.9.20**
- **Gradle 8.4**

## 🔧 Installation

### Prérequis
- Android Studio Hedgehog ou plus récent
- JDK 21
- Android SDK avec API 34

### Compilation
```bash
git clone https://github.com/kapoue/NeverForget.git
cd NeverForget
./gradlew assembleRelease
```

### APK
L'APK de release sera disponible dans `app/build/outputs/apk/release/`

## 📋 Permissions requises

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

## 🧪 Tests

### Tests unitaires inclus
- `CompleteTaskUseCaseTest` : Logique de validation des tâches
- `ValidateTaskFormUseCaseTest` : Validation des formulaires
- `NotificationSchedulerTest` : Planification des notifications
- `CategoryHelperTest` : Gestion des catégories
- `DateUtilsTest` : Utilitaires de dates

### Exécution des tests
```bash
./gradlew test
```

## 🐛 Problèmes connus

Aucun problème critique connu dans cette version.

## 🔮 Roadmap v2.0

### Fonctionnalités prévues
- **Thème sombre/clair** automatique
- **Statistiques** d'utilisation et de réalisation
- **Widget Android** pour l'écran d'accueil
- **Synchronisation cloud** automatique
- **Support multilingue** (anglais, etc.)
- **Notifications personnalisées** (sons, vibrations)

## 👨‍💻 Développement

### Contribution
Les contributions sont les bienvenues ! Consultez le guide de contribution dans le repository.

### Structure du projet
```
NeverForget/
├── app/src/main/java/com/neverforget/
│   ├── data/          # Room, Repository, Models
│   ├── domain/        # Use Cases, Business Logic
│   ├── ui/            # Screens, ViewModels, Components
│   ├── workers/       # Background Tasks
│   ├── receivers/     # Broadcast Receivers
│   ├── notifications/ # Notification System
│   └── utils/         # Utilities, Helpers
├── app/src/test/      # Unit Tests
└── docs/              # Documentation
```

## 📄 Licence

Ce projet est sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.

## 🙏 Remerciements

Merci à la communauté Android et aux mainteneurs des bibliothèques open source utilisées dans ce projet.

---

**Version** : 1.0.0  
**Date de release** : 24 octobre 2025  
**Taille de l'APK** : ~8 MB  
**Développeur** : Kapoue  

🔗 **Liens utiles**
- [Repository GitHub](https://github.com/kapoue/NeverForget)
- [Issues & Bug Reports](https://github.com/kapoue/NeverForget/issues)
- [Documentation technique](https://github.com/kapoue/NeverForget/wiki)