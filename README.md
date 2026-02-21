## 📋 Overview

This API implements an automated retirement savings system that rounds up expenses to the next multiple of 100 and invests the difference (remanent) with various rules and calculations. The system handles complex temporal constraints, validates financial transactions, calculates investment returns across multiple investment vehicles, and provides inflation-adjusted projections.

**Live API URL:** [https://blackrockchallenge.onrender.com](https://blackrockchallenge.onrender.com)

**GitHub Repository:** [https://github.com/RKSAHOO4414/BlackRockChallenge](https://github.com/RKSAHOO4414/BlackRockChallenge)

---

## 🛠️ Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| Framework | Spring Boot | 3.2.5 |
| Language | Java | 17 |
| Database | PostgreSQL | 16 |
| Build Tool | Maven | 3.9.6 |
| Container | Docker | Latest |
| Deployment | Render | Cloud |
| Testing | JUnit | 5 |

---



## 🔌 API Endpoints

| # | Endpoint | Method | Description |
|---|----------|--------|-------------|
| 1 | `/blackrock/challenge/v1/transactions/parse` | POST | Parse expenses and calculate ceiling & remanent |
| 2 | `/blackrock/challenge/v1/transactions/validator` | POST | Validate transactions against wage limits |
| 3 | `/blackrock/challenge/v1/transactions/filter` | POST | Filter transactions by temporal periods (q/p/k) |
| 4 | `/blackrock/challenge/v1/returns/nps` | POST | Calculate NPS returns with tax benefits |
| 5 | `/blackrock/challenge/v1/returns/index` | POST | Calculate Index Fund returns |

---

## 🚀 Getting Started

### Prerequisites

- **Java 17** or higher
- **Maven 3.9+**
- **Docker** (optional, for containerization)
- **PostgreSQL 16** (optional, for local development)
- **Git**

---

## 📦 Installation & Setup

### Option 1: Run Locally (Without Docker)

#### Step 1: Clone the Repository
```bash
git clone https://github.com/RKSAHOO4414/BlackRockChallenge.git
cd BlackRockChallenge




