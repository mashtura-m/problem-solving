# Agentic System Design for Software Engineers
### A Production AI Agent Architecture Reference

> **Audience:** Senior Software Engineers  
> **Use Case:** E-Commerce Shopping Agent — Customer Query → Product Recommendation → Cart → Checkout → DB Update  
> **Stack Coverage:** LLM · RAG · Tools · Memory · Guardrails · Observability · Deployment

---

## Table of Contents

1. [Foundations — What is an Agentic System?](#1-foundations--what-is-an-agentic-system)
2. [Traditional Software Design vs. Agentic System Design](#2-traditional-software-design-vs-agentic-system-design)
3. [Anatomy of a Production AI Agent](#3-anatomy-of-a-production-ai-agent)
4. [The Agent Reasoning Loop](#4-the-agent-reasoning-loop)
5. [Context Engineering](#5-context-engineering)
6. [E-Commerce Use Case — Shopping Agent](#6-e-commerce-use-case--shopping-agent)
7. [Agent Development Lifecycle (ADLC)](#7-agent-development-lifecycle-adlc)
8. [Step-by-Step Developer Workflow](#8-step-by-step-developer-workflow)
9. [Production Readiness Checklist](#9-production-readiness-checklist)

---

## 1. Foundations — What is an Agentic System?

An **AI Agent** is not a chatbot with memory. It is an autonomous reasoning engine that:
- **Perceives** its environment (user input, tool results, retrieved documents)
- **Decides** which actions to take next
- **Executes** those actions via tools (real API/DB calls)
- **Iterates** toward a goal — without a human directing every step

### Three Tiers of Agentic Systems

| Tier | Description |
|---|---|
| 🤖 **Traditional Chatbot** | Input → LLM → Output. One turn. No memory beyond window. No action capability. No goal pursuit. |
| ⚡ **AI Agent** | Perceive → Think → Plan → Act → Observe → Repeat. Multi-turn, goal-driven, tool-calling, stateful. |
| 🕸️ **Multi-Agent System** | Orchestrator delegates to specialist agents (search agent, DB agent, checkout agent). Each owns a domain. |

---

## 2. Traditional Software Design vs. Agentic System Design

Both solve business problems. But the architecture, control flow, and failure modes are fundamentally different.

| Dimension | Traditional Software System | Agentic AI System |
|---|---|---|
| **Control Flow** | Deterministic. Code dictates every branch. You own the flow graph completely. | Semi-deterministic. LLM decides next action at runtime. You design guardrails, not exact paths. |
| **State Management** | Stored in DB, session, cache. Explicit reads/writes by you. | State = context window + external memory. Agent manages its own working memory each call. |
| **Input Handling** | Typed, validated, structured schema (REST/gRPC). | Natural language (unstructured). Intent must be inferred and extracted before acting. |
| **Business Logic** | Hardcoded in functions, classes, services. | Expressed as instructions in system prompt + few-shot examples + tool definitions. |
| **Integration** | REST APIs, DB drivers, message queues. Explicit contracts. | Tools (functions the LLM can call). MCP servers. Agent decides when/how to invoke them. |
| **Error Handling** | try/catch, circuit breakers, retry logic in code. | Error fed back into context. Agent reasons about failure and self-corrects. You set retry limits. |
| **Testing** | Unit tests, integration tests, E2E. Pass/fail is deterministic. | Eval frameworks (LLM-as-judge, human eval, golden datasets). Probabilistic correctness. |
| **Deployment Unit** | Microservice, container, serverless function. | Agent = LLM + system prompt + tools + memory. Deployed as a reasoning loop service. |
| **Scalability** | Horizontal scaling of stateless services. | Parallelise agent instances. Context window is the bottleneck, not compute. |
| **Observability** | Logs, metrics, traces (OpenTelemetry). | Token traces, tool call logs, context snapshots, latency per reasoning step. |
| **Security** | Auth, RBAC, input sanitisation, SQL injection prevention. | Prompt injection, tool misuse guardrails, PII redaction, consent gates before side effects. |
| **Failure Mode** | Exception, null pointer, timeout, race condition. | Hallucination, context rot, infinite loops, tool misuse, goal misalignment. |

### Traditional Software Flow (Deterministic, Linear)

```
User Request (HTTP)
      │
      ▼
 API Gateway ──► Business Logic ──► Data Layer ──► Response
 (Auth/Route)    (Service Layer)    (ORM/Query)    (JSON/HTML)
```

### Agent System Flow (Non-linear, Goal-driven, Iterative)

```
User Input (Natural Language)
      │
      ▼
Context Assembly ─────────────────────────────────────────────────────────┐
(System Prompt + Memory + RAG + User Message)                             │
      │                                                                   │
      ▼                                                                   │
LLM Reasoning Engine                                                      │
(Parse intent → Check tools → Decide action)                              │
      │                                                                   │
      ├─── Tool Call Needed? ──► Execute Tool ──► Observe Result ─────────┘
      │                         (API/DB/Search)   (Inject into context,
      │                                            loop again)
      │
      └─── Final Answer? ──► Response to User (Natural Language)
```

> **Key insight:** In traditional software, YOU are the control flow. In an agent, the LLM is the control flow. Your job is to engineer the environment it operates in.

---

## 3. Anatomy of a Production AI Agent

Every production agent is composed of **eight core components**.

### Component Overview

| Component | Role | Software Analogy |
|---|---|---|
| 🧠 **LLM (Reasoner)** | Reads context, reasons, decides next action | CPU — processes, but doesn't store state |
| 📋 **System Prompt** | Role, tone, rules, constraints, tool instructions | Config + Business Logic combined |
| 🔧 **Tools** | Functions the LLM can invoke (search, DB, cart) | Connectors / Service integrations |
| 💾 **Memory System** | Short-term (context) + Long-term (vector DB) | Cache (Redis) + Data Warehouse |
| 🔍 **RAG Pipeline** | Inject relevant knowledge at runtime | Dynamic data source lookup |
| 🛡️ **Guardrails** | Safety, consent gates, PII, loop limits | Input validation + Auth middleware |
| 📊 **Observability** | Trace every step, token, tool call | OpenTelemetry + APM |
| 🔄 **Orchestrator** | Manages the loop, routes sub-tasks, handles retries | Service mesh / Job scheduler |

---

### Layered Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│  INTERFACE LAYER                                                │
│  Web Chat Widget │ Mobile SDK │ WhatsApp │ REST API │ Voice     │
└───────────────────────────────┬─────────────────────────────────┘
                                │
┌───────────────────────────────▼─────────────────────────────────┐
│  ORCHESTRATION LAYER                                            │
│  Agent Loop Controller │ Context Assembler │ Tool Router        │
│  Retry Manager │ Guardrail Engine │ Multi-agent Orchestrator    │
└───────────────────────────────┬─────────────────────────────────┘
                                │
┌───────────────────────────────▼─────────────────────────────────┐
│  INTELLIGENCE LAYER                                             │
│  LLM (Claude / GPT-4o) │ System Prompt │ RAG Retriever         │
│  Few-shot Examples │ Intent Classifier │ Embedding Model        │
└───────────────────────────────┬─────────────────────────────────┘
                                │
┌───────────────────────────────▼─────────────────────────────────┐
│  TOOL LAYER                                                     │
│  search_products() │ get_recommendations() │ add_to_cart()      │
│  checkout() │ update_inventory() │ get_order_status()           │
└───────────────────────────────┬─────────────────────────────────┘
                                │
┌───────────────────────────────▼─────────────────────────────────┐
│  DATA LAYER                                                     │
│  Vector DB (Product Embeddings) │ Redis (Conversation State)    │
│  PostgreSQL (Inventory / Orders) │ Analytics / Event Stream     │
└─────────────────────────────────────────────────────────────────┘
```

---

## 4. The Agent Reasoning Loop

This is the heartbeat of every agent. Unlike a REST handler that runs once, the agent loop iterates until it reaches a stopping condition.

```
                    ┌──────────────────────────────────┐
                    │         CONTEXT WINDOW           │
                    │  System Prompt                   │
                    │  + Conversation History          │
                    │  + RAG Retrieved Docs            │
                    │  + Tool Results                  │
                    │  + Current User Message          │
                    └────────────────┬─────────────────┘
                                     │
                                     ▼
                    ┌──────────────────────────────────┐
                    │      LLM REASONING ENGINE        │
                    │                                  │
                    │  Step 1: Parse user intent       │
                    │  Step 2: Check available tools   │
                    │  Step 3: Decide → tool OR answer │
                    │  Step 4: Generate action         │
                    │  Step 5: Observe → loop or stop  │
                    └────────┬─────────────┬───────────┘
                             │             │
               Tool needed?  │             │  Final answer?
                             ▼             ▼
               ┌─────────────────┐   ┌────────────────┐
               │  TOOL EXECUTE   │   │  RESPOND USER  │
               │  (API/DB call)  │   │  (Natural lang)│
               └────────┬────────┘   └────────────────┘
                        │
                        │ Observe result
                        │ Inject into context
                        └──────────────────────────────► Loop again
```

**Pseudocode:**
```python
def agent_loop(user_input, session):
    context = assemble_context(session, user_input)
    iterations = 0

    while iterations < MAX_ITERATIONS:
        response = llm.call(context)

        if response.is_final_answer:
            store_to_memory(session, response)
            return response.text

        if response.is_tool_call:
            result = execute_tool(response.tool_name, response.tool_args)
            context = inject_tool_result(context, result)

        iterations += 1

    return escalate_to_human(session)
```

### Stop Conditions
- ✅ Goal achieved / user request fulfilled
- ✅ User responded and confirmed
- ⛔ Max iterations reached (prevent infinite loops)
- ⛔ Tool failure after N retries → human escalation
- ⛔ Guardrail triggered

---

## 5. Context Engineering

> **Andrej Karpathy:** *"The LLM is the CPU, the context window is the RAM."*

The context window is the agent's working memory — finite, precious, and directly tied to output quality. More tokens ≠ better output. A 2025 Chroma study showed models can drop from 95% to 60% accuracy once context crosses a threshold.

### Context Window Composition — E-Commerce Agent (128K tokens)

```
┌────────┬───────┬───────────────┬──────────────────┬──────────┬──────────────┬────────────┐
│ System │Persona│  RAG Products │ Conversation Hist│Tool Defs │ Tool Results │  FREE      │
│  12%   │  8%   │     18%       │       22%        │   10%    │     20%      │   10%      │
│ 1,500  │  900  │    2,200      │      2,800       │  1,200   │    2,500     │115,600 tok │
└────────┴───────┴───────────────┴──────────────────┴──────────┴──────────────┴────────────┘
                                  Total used: ~12,400 / 128,000 tokens
```

### The Four Context Strategies

| Strategy | What it means | ETL Analogy |
|---|---|---|
| ✍️ **Write** | Persist tool results and decisions to external memory | Write to staging table |
| 🔎 **Select** | RAG: retrieve only the K most relevant chunks | Predicate pushdown |
| 🗜️ **Compress** | Summarise old turns; truncate verbose outputs | Aggregate before load |
| 🔒 **Isolate** | Specialist agents get only their context slice | Schema separation |

### Context Engineering Rules

1. **Never stuff the context window.** Every irrelevant token steals attention from what matters.
2. **Measure token usage per call.** Alert at 80% capacity.
3. **Compress conversation history** after 10+ turns — summarise, don't truncate blindly.
4. **Retrieve, don't pre-load.** Use RAG to inject product knowledge dynamically, not the whole catalog statically.
5. **Isolate sub-agent contexts.** The search agent doesn't need the checkout agent's state.

---

## 6. E-Commerce Use Case — Shopping Agent

**Goal:** A caring, friendly AI agent that understands customer intent, recommends products from live inventory, motivates purchase, handles cart & checkout with consent, and updates the DB in real-time.

### Customer Journey — 8 Steps

```
STEP 01        STEP 02        STEP 03        STEP 04
👋 Greet   →  🔍 Search   →  ⭐ Recommend →  💬 Motivate
& Understand   & Retrieve     Top 3 Products  Handle Q&A

STEP 05        STEP 06        STEP 07        STEP 08
✅ Consent  →  🛒 Add to   →  💳 Checkout  →  📊 Update
Gate           Cart            & Confirm       DB/Inventory
```

### Agent Architecture — E-Commerce

```
                    ┌──────────────────────────────────────┐
                    │     🤖 SHOPPING ASSISTANT AGENT      │
                    │  Model: claude-sonnet-4              │
                    │  Tone: caring, friendly, helpful     │
                    │                                      │
                    │  TOOLS:                              │
                    │  • search_products(query, filters)   │
                    │  • get_recommendations(user_prefs)   │
                    │  • get_product_detail(id)            │
                    │  • check_stock(product_id)           │
                    │  • add_to_cart(product_id, qty)      │
                    │  • remove_from_cart(item_id)         │
                    │  • get_cart_summary()                │
                    │  • checkout(payment_method)          │
                    │  • update_inventory(product_id,delta)│
                    │  • get_order_status(order_id)        │
                    └───────┬──────────────────────────────┘
                            │ connects to
        ┌───────────────────┼─────────────────────┐
        │                   │                     │
        ▼                   ▼                     ▼
┌───────────────┐  ┌────────────────┐  ┌──────────────────┐
│ 📚 Product    │  │ 📦 Inventory   │  │ 🛒 Cart / Order  │
│ Vector DB     │  │ System         │  │ DB               │
│ (Semantic     │  │ (Live stock,   │  │ (Session cart,   │
│  search)      │  │  real-time     │  │  order records)  │
└───────────────┘  │  updates)      │  └──────────────────┘
                   └────────────────┘

        ┌───────────────────┬─────────────────────┐
        ▼                   ▼                     ▼
┌───────────────┐  ┌────────────────┐  ┌──────────────────┐
│ 💾 Conv Store │  │ 🛡️ Guardrails  │  │ 📊 Observability │
│ (Redis, per-  │  │ (Consent gate, │  │ (Langfuse traces,│
│  session)     │  │  PII filter,   │  │  token tracking) │
└───────────────┘  │  3 retries max)│  └──────────────────┘
                   └────────────────┘
```

### System Prompt Template

```
## ROLE
You are a warm, caring shopping assistant for [Store Name].
Your goal is to genuinely help customers find the perfect product — not just sell.

## TONE
- Always friendly, encouraging, and empathetic
- Use light, appropriate emoji to express warmth
- Never pushy — motivate through genuine value, not pressure
- Celebrate customer decisions with positivity

## TOOLS — WHEN TO USE
- search_products(): When customer describes what they want
- get_product_detail(): When customer asks specifics about a product
- add_to_cart(): ONLY after explicit customer confirmation ("yes", "add it", "go ahead")
- checkout(): ONLY after add_to_cart succeeds AND customer confirms checkout intent
- update_inventory(): Call immediately after every successful checkout

## GUARDRAILS
- NEVER add to cart or checkout without explicit consent
- NEVER make up product details — use get_product_detail() if unsure
- If stock is 0, suggest alternatives — never promise unavailable items
- Max 3 tool call retries before escalating to human agent

## GOAL
Help customer find the right product → motivate through genuine value
→ complete purchase with consent → ensure DB is updated.
```

### Live Conversation Trace

```
👤 CUSTOMER:
"I'm looking for a good laptop for my daughter who just started university.
She'll use it for design and some gaming. Budget around $1200."

⚙ AGENT INTERNALS:
  INTENT EXTRACTED:
    category=laptop | use_case=design,gaming | budget=$1200 | relation=gift
  TOOL CALL:
    search_products(query="laptop design gaming student", max_price=1200)
  RESULT:
    [ASUS ProArt $1149, Dell XPS 15 $1199, MSI Creator 15 $1099]

🤖 SHOPPING AGENT:
"How lovely! Starting university is such an exciting milestone — and you
want to make sure she has the right tools to shine 🎉

I've found three laptops that balance design performance and gaming really
well within your budget:

1. ASUS ProArt Studiobook 16 — $1,149 ✨
   OLED display (perfect for design), RTX 3060, 16GB RAM. In stock (12 units).

2. Dell XPS 15 — $1,199
   OLED display, best battery life. In stock (5 units).

3. MSI Creator 15 — $1,099
   RTX 3060, 144Hz display, strong for both design and gaming. In stock (8 units).

Would you like more detail on any of these? 💙"

---

👤 CUSTOMER:
"The ASUS one sounds great. Does it come with a warranty?"

⚙ AGENT INTERNALS:
  TOOL CALL: get_product_detail(product_id="ASUS-PROART-16")
  RESULT: {warranty: "2-year international", weight: "2.0kg", display: "16in OLED 4K"}

🤖 SHOPPING AGENT:
"Great choice! The ASUS ProArt comes with a 2-year international warranty —
perfect for a university student who might be travelling or studying abroad.
It weighs just 2.0kg and the 4K OLED screen will make her design work look
absolutely stunning 🌟

At $1,149 it's also $51 under budget. Shall I add it to your cart?"

---

👤 CUSTOMER:
"Yes please, add it and let's checkout."

⚙ AGENT INTERNALS:
  CONSENT CONFIRMED ✓
  TOOL CALL: add_to_cart(product_id="ASUS-PROART-16", qty=1)
  RESULT: {cart_id: "C-8821", total: "$1,149"}

  TOOL CALL: checkout(cart_id="C-8821")
  RESULT: {order_id: "ORD-44192", status: "confirmed"}

  TOOL CALL: update_inventory(product_id="ASUS-PROART-16", delta=-1)
  RESULT: {remaining_stock: 11}

🤖 SHOPPING AGENT:
"You're all set! 🎉 Order confirmed — Order #ORD-44192.
You'll receive a confirmation email shortly. The ASUS ProArt Studiobook 16
is on its way to make your daughter's university journey even more amazing.
She's going to love it! 💙

Is there anything else I can help you with today?"
```

### Tool Schema Example (JSON)

```json
{
  "name": "add_to_cart",
  "description": "Adds a product to the customer's cart. ONLY call this after the customer has given explicit verbal confirmation that they want to add the item. Never call speculatively.",
  "parameters": {
    "type": "object",
    "properties": {
      "product_id": {
        "type": "string",
        "description": "The unique product identifier from the inventory system"
      },
      "quantity": {
        "type": "integer",
        "description": "Number of units to add. Default to 1 if not specified by customer.",
        "default": 1
      }
    },
    "required": ["product_id"]
  },
  "returns": {
    "cart_id": "string",
    "item_added": "object",
    "cart_total": "string"
  }
}
```

> ⚠️ **Tool description quality is critical.** The LLM reads the `description` field to decide when and whether to call a tool. A vague description leads to misuse. A precise description is a guardrail.

---

## 7. Agent Development Lifecycle (ADLC)

Unlike traditional SDLC where you build → test → ship, agent development is **iterative, eval-driven, and never truly "done."**

```
┌────────────┐    ┌────────────┐    ┌────────────┐
│  PHASE 01  │    │  PHASE 02  │    │  PHASE 03  │
│  Problem   │──► │Architecture│──► │  Prompt    │
│  Scoping   │    │  Design    │    │Engineering │
└────────────┘    └────────────┘    └─────┬──────┘
                                          │
┌────────────┐    ┌────────────┐    ┌─────▼──────┐
│  PHASE 06  │    │  PHASE 05  │    │  PHASE 04  │
│  Deploy &  │◄── │  Eval &    │◄── │  Build &   │
│  Monitor   │    │  Red-team  │    │  Integrate │
└────────────┘    └────────────┘    └────────────┘
      │
      └──────────────────────────────────────────► Continuous Improvement Loop
```

### Phase Details

#### Phase 01 — Problem Scoping
- Define agent goal precisely (not "help customers" — be specific)
- Map every user intent type (browse, compare, buy, track order, return)
- Identify all required tools and external systems
- Define measurable success metrics (CSAT score, conversion rate, resolution rate, avg session cost)
- **Set hard scope boundaries — what will the agent NOT do?**

#### Phase 02 — Architecture Design
- Choose LLM model (capability vs. cost — start with Sonnet, not Opus)
- Design tool schema (name, description, parameters, return shape)
- Plan memory strategy (Redis for short-term, pgvector for long-term)
- Define context window budget per call
- Design guardrails and consent gate logic
- Plan multi-agent topology if needed

#### Phase 03 — Prompt Engineering
- Write system prompt: role → tone → tool usage rules → guardrails
- Add 5-10 few-shot examples covering ambiguous or edge-case intents
- Define intent taxonomy (browse / compare / buy / track / escalate)
- Test manually in Anthropic Workbench or OpenAI Playground
- Log every failure case — these become your eval dataset

#### Phase 04 — Build & Integrate
- Implement tool functions as real API/DB calls (not mocks)
- Build agent loop: call LLM → parse tool call → execute → inject → repeat
- Wire up RAG pipeline: embed catalog → index → semantic search
- Implement memory store with compression logic
- Add observability hooks on every LLM and tool call

#### Phase 05 — Eval & Red-Team
- Build golden test dataset (50+ cases minimum)
- Categories: happy path, ambiguous intent, out-of-stock, consent refusal, multi-turn correction
- Run LLM-as-judge evals for quality scoring
- Red-team for prompt injection, jailbreaks, tool misuse
- Measure: accuracy, cost-per-session, avg turns to resolution, tool call error rate

#### Phase 06 — Deploy & Monitor
- Canary deploy to 5% of traffic
- Monitor: CSAT, conversion rate, error rate, cost per session, avg turns
- A/B test prompt variants simultaneously
- Set up human escalation for failed conversations
- Schedule weekly prompt review cadence

---

## 8. Step-by-Step Developer Workflow

### Week 1 — Scoping

**Step 1: Define Agent Contract**

Write an "Agent Brief" document:
- Who it serves (customer segment)
- What it can do (list of permitted actions)
- What it cannot do (hard scope boundaries)
- Which tools it has access to
- What the success metric is

**Step 2: Design Tool Schema**

Define every tool as a JSON schema. The description field is critical — the LLM reads it to decide when to call the tool.

```
Tool Name        │ When to Call              │ Side Effect?
─────────────────┼───────────────────────────┼─────────────
search_products  │ Customer describes need   │ No (read)
get_product_detail│ Customer asks specifics  │ No (read)
check_stock      │ Before recommending       │ No (read)
add_to_cart      │ After EXPLICIT consent    │ YES ⚠️
checkout         │ After add_to_cart + consent│ YES ⚠️
update_inventory │ After successful checkout │ YES ⚠️
```

### Week 2 — Prompt Engineering

**Step 3: Write & Iterate System Prompt**
- Start with role + tone + tool usage rules + guardrails
- Run manual tests in Workbench / Playground
- Log every failure — add few-shot examples for recurring patterns
- Version control your prompt in Git like production code

### Weeks 2–3 — Build

**Step 4: Implement Tools & Agent Loop**

```python
async def agent_loop(user_message: str, session_id: str) -> str:
    context = await assemble_context(session_id, user_message)
    max_iter = 10

    for i in range(max_iter):
        response = await llm.call(
            model="claude-sonnet-4",
            messages=context,
            tools=TOOL_SCHEMAS
        )

        if response.stop_reason == "end_turn":
            await save_to_memory(session_id, response.text)
            return response.text

        if response.stop_reason == "tool_use":
            for tool_call in response.tool_calls:
                # Consent gate for write operations
                if tool_call.name in WRITE_TOOLS:
                    if not await verify_user_consent(session_id, tool_call):
                        return "I need your explicit confirmation before I can do that."

                result = await execute_tool(tool_call.name, tool_call.input)
                context = inject_tool_result(context, tool_call.id, result)

    return await escalate_to_human(session_id)
```

**Step 5: Build RAG Pipeline**
```
Product Catalog (raw text/JSON)
        │
        ▼
Embedding Model (text-embedding-3-small)
        │
        ▼
Vector Index (pgvector / Pinecone)
        │  At runtime:
        ▼
User Query → Embed → Cosine Similarity Search → Top-K Products → Inject into context
```

### Week 3 — Memory & Context

**Step 6: Add Memory & Context Management**
- Wire Redis for short-term conversation state (per session_id)
- Implement context compression after N turns
- Add token counter per call — alert at 80% capacity

```python
def compress_history(messages: list, threshold: int = 10) -> list:
    if len(messages) <= threshold:
        return messages
    
    old_messages = messages[:-threshold]
    recent_messages = messages[-threshold:]
    
    summary = llm.summarise(old_messages)  # compress old turns
    
    return [{"role": "system", "content": f"Earlier conversation summary: {summary}"}] + recent_messages
```

### Week 4 — Safety & Observability

**Step 7: Implement Guardrails & Consent Logic**
- Build consent gate: explicit confirmation before any write tool
- Add input sanitisation for prompt injection patterns
- Add PII redaction before storing conversation logs
- Add output validators for factual claims

**Step 8: Add Tracing & Monitoring**

Instrument every call:
```python
@trace(name="agent_tool_call")
async def execute_tool(tool_name: str, args: dict) -> dict:
    span = tracer.start_span(tool_name)
    span.set_attribute("tool.name", tool_name)
    span.set_attribute("tool.args", json.dumps(args))
    
    start = time.time()
    result = await TOOLS[tool_name](**args)
    
    span.set_attribute("tool.latency_ms", (time.time() - start) * 1000)
    span.set_attribute("tool.success", True)
    span.end()
    return result
```

### Week 5 — Evaluation

**Step 9: Build Eval Suite & Red-Team**

Golden test case categories:

| Category | Count | What to test |
|---|---|---|
| Happy path | 15 | Full journey: browse → buy → checkout |
| Ambiguous intent | 10 | Vague queries, multiple intents at once |
| Out-of-stock | 8 | Agent suggests alternatives gracefully |
| Consent refusal | 5 | Customer says no — agent respects it |
| Multi-turn correction | 7 | Customer changes mind mid-conversation |
| Prompt injection | 5 | Adversarial inputs to test guardrails |
| Edge cases | 10 | Budget mismatch, unknown product, DB error |

### Week 6 — Deploy

**Step 10: Canary Deployment**
- Deploy to 5% of traffic
- Monitor for 48 hours before ramping
- Keep traditional service as human fallback on 100% traffic

### Ongoing — Operations

**Step 11: Continuous Improvement Loop**
- Review 10 failed conversations weekly
- Add few-shot examples for recurring failure patterns
- Refresh product embeddings as catalog changes
- Refine tool descriptions where misuse is observed
- Track cost per session trend — compression is your lever

---

## 9. Production Readiness Checklist

> Before your agent goes live, validate every dimension. This separates a demo from a production-grade system.

### 🧠 Intelligence
- [ ] System prompt reviewed by domain expert
- [ ] Few-shot examples cover top 10 intent patterns
- [ ] RAG retrieval quality score > 85%
- [ ] LLM eval suite passing > 90%
- [ ] Fallback for ambiguous or out-of-scope input

### 🛡️ Safety & Guardrails
- [ ] Consent gate before ALL side-effect tools (add_to_cart, checkout, update_inventory)
- [ ] Prompt injection tests passing (red-team complete)
- [ ] PII redacted before storage
- [ ] Max iteration limit enforced (e.g., 10 steps)
- [ ] Human escalation path tested and working

### ⚡ Performance
- [ ] P95 response time < 4 seconds
- [ ] Context window < 80% used per call
- [ ] Cost per conversation budgeted and monitored
- [ ] Tool timeout handling implemented (circuit breaker)
- [ ] Context compression tested at 10+ turn sessions

### 📊 Observability
- [ ] Every tool call logged with trace ID
- [ ] Token usage tracked per call and session
- [ ] Dashboard live: CSAT, conversion rate, error rate
- [ ] Alerts configured: error rate > 5%, cost spike
- [ ] Failed conversation review process defined

### 🔧 Reliability
- [ ] Tool error → agent self-correction tested
- [ ] DB/inventory update is transactional (no partial writes)
- [ ] Out-of-stock scenario handled gracefully (alternative suggestions)
- [ ] Retry logic with exponential backoff on tool failure
- [ ] Idempotent checkout (no double-orders on retry)

### 🚀 Deployment
- [ ] Canary deployment configured (5% traffic)
- [ ] Rollback plan documented and tested
- [ ] A/B testing framework for prompt variants
- [ ] API rate limits configured on LLM provider
- [ ] Weekly prompt review cadence scheduled

---

## 🔑 The 5 Golden Rules of Production Agent Design

### Rule 01 — Context is RAM. Treat it as a precious resource.
Never stuff the context window. Select only what's relevant, compress what's stale, isolate sub-agent contexts. More tokens ≠ better output. A 2025 Chroma study proved all 18 tested frontier models degrade with context overload.

### Rule 02 — Consent before every side effect.
Read operations are safe. Write operations (add_to_cart, checkout, update_inventory) **always** require explicit user confirmation before calling. Build the consent gate into the orchestrator, not just the prompt.

### Rule 03 — Your system prompt IS your business logic.
Every rule you'd write as an `if` statement belongs in the prompt. Version control it. Code-review it. Ship no prompt change without running your eval suite. The prompt is a production artifact.

### Rule 04 — Evals replace unit tests.
Agent output is probabilistic — you cannot assert exact string equality. Build a golden dataset. Use LLM-as-judge for quality scoring. Set minimum pass thresholds. Run evals on every prompt change before merging.

### Rule 05 — Always have a human fallback.
Design the escalation path on day one, not as an afterthought. Agents that fail silently destroy customer trust. Agents that escalate gracefully ("Let me connect you with a specialist") build it. Wire the fallback before you go live.

---

## Quick Reference — Tech Stack

| Layer | Technology Options |
|---|---|
| **LLM** | Claude Sonnet 4 · GPT-4o · Gemini Pro |
| **Orchestration** | LangGraph · CrewAI · Custom Python loop |
| **RAG / Embeddings** | LlamaIndex · LangChain · text-embedding-3-small |
| **Vector DB** | pgvector · Pinecone · Weaviate · Chroma |
| **Short-term Memory** | Redis · In-memory dict |
| **Long-term Memory** | PostgreSQL · DynamoDB |
| **Observability** | Langfuse · LangSmith · OpenTelemetry |
| **Guardrails** | NeMo Guardrails · Custom validators |
| **Evals** | Braintrust · Promptfoo · Custom LLM-as-judge |
| **Deployment** | Docker · Kubernetes · AWS Lambda · Modal |

---

*Agentic System Design Reference · For Software Engineers · Production-Grade AI Agent Architecture*  
*Context Engineering × Tool Design × Agent Lifecycle × E-Commerce Use Case*

