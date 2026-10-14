import { apiClient } from "./api";
import { ChatRequest, ChatResponse } from "../types";

export const chatService = {
  /**
   * Sends a user question to the version-aware RAG engine and Groq LLM
   */
  async sendMessage(question: string): Promise<ChatResponse> {
    return this.sendQuery({ question });
  },

  async sendQuery(payload: ChatRequest): Promise<ChatResponse> {
    try {
      const parsedVer = payload.versionNumber
        ? parseInt(String(payload.versionNumber).replace(/[^0-9]/g, ''), 10)
        : null;

      const backendResp = await apiClient.post<{
        status: string;
        message: string;
        data: {
          answer: string;
          documentName?: string;
          versionNumber?: number;
          effectiveDate?: string;
          currentVersion?: string;
          olderVersion?: string;
          conflictDescription?: string;
          warning?: string;
          conflict?: {
            currentVersion?: string;
            olderVersion?: string;
            conflictDescription?: string;
            warning?: string;
          };
          query?: string;
          resolvedVersion?: string;
          sourceChunks?: Array<{
            id?: string;
            documentName: string;
            versionNumber: number | string;
            effectiveDate: string;
            department?: string;
            chunkText?: string;
            score?: number;
          }>;
          latencyMs?: number;
        };
      }>("/api/v1/rag/query", {
        query: payload.question,
        documentName: payload.documentName || null,
        versionNumber: parsedVer,
      });

      const data = backendResp.data?.data || backendResp.data;

      return {
        answer: data.answer || "No answer generated.",
        documentName: data.documentName || payload.documentName || "Enterprise Document",
        version: data.versionNumber || data.resolvedVersion || 1,
        effectiveDate: data.effectiveDate || new Date().toISOString().split('T')[0],
        department: data.sourceChunks?.[0]?.department || "Enterprise",
        confidence: data.sourceChunks?.[0]?.score || 0.95,
        retrievedChunks: (data.sourceChunks || []).map((c) => ({
          id: c.id || String(Math.random()),
          documentName: c.documentName,
          versionNumber: c.versionNumber,
          effectiveDate: c.effectiveDate,
          department: c.department,
          chunkText: c.chunkText || '',
          similarityScore: c.score,
        })),
        responseTime: data.latencyMs || 250,
        conflict: Boolean(data.conflict || data.warning),
        conflictDetails: data.conflict
          ? {
              currentVersion: data.conflict.currentVersion || "v2",
              olderVersion: data.conflict.olderVersion || "v1",
              conflictDescription:
                data.conflict.conflictDescription || "Contradictory values detected between versions.",
              reasonSelected: "Latest active version resolved via effective date and version hierarchy.",
            }
          : undefined,
        warning: data.warning,
      };
    } catch (err: any) {
      console.error("[ChatService] Backend query error:", err);
      const errMsg =
        err?.response?.data?.message ||
        err?.response?.data?.error ||
        err?.message ||
        "Backend unreachable on port 8081";

      throw new Error(errMsg);
    }
  },
};

/**
 * High-fidelity fallback simulating realistic enterprise version-aware RAG responses and conflict detection
 */
function simulateVersionAwareResponse(question: string): ChatResponse {
  const q = question.toLowerCase();

  if (q.includes("password") || q.includes("rotation") || q.includes("credential") || q.includes("expire")) {
    return {
      answer:
        "According to the current Enterprise Password & Security Policy (v2, effective January 15, 2024), all user passwords and API credentials must be rotated every 90 days. In addition, multi-factor authentication (MFA) is strictly mandatory for all internal and external network access.",
      documentName: "Enterprise Password & Security Policy",
      version: 2,
      effectiveDate: "2024-01-15",
      department: "Information Security",
      confidence: 0.945,
      responseTime: 184,
      conflict: true,
      conflictDetails: {
        currentVersion: "v2 (2024-01-15)",
        olderVersion: "v1 (2023-01-01)",
        conflictDescription:
          "Version 1 required 30-day password rotation without mandatory MFA. Current Version 2 extended rotation to 90 days while enforcing hardware-backed MFA.",
        reasonSelected:
          "Version 2 was selected because it has the highest version number (v2 > v1) and the most recent effective date (2024-01-15 > 2023-01-01). Older version chunks were filtered out prior to LLM generation.",
      },
      warning:
        "Conflict detected: Older version 1 specified 30-day rotation. Answer was strictly grounded on active version 2.",
      retrievedChunks: [
        {
          id: "chk-1",
          documentName: "Enterprise Password & Security Policy",
          versionNumber: 2,
          effectiveDate: "2024-01-15",
          department: "Information Security",
          chunkText:
            "Section 4.1: Password Lifespan and Renewal. All employee domain passwords, SSO credentials, and API access keys must undergo mandatory rotation every 90 calendar days. Phishing-resistant MFA (FIDO2/WebAuthn) is enforced.",
          similarityScore: 0.945,
        },
        {
          id: "chk-2",
          documentName: "Enterprise Password & Security Policy",
          versionNumber: 2,
          effectiveDate: "2024-01-15",
          department: "Information Security",
          chunkText:
            "Section 4.2: Password Complexity Requirements. Passwords must be a minimum of 16 characters in length, containing uppercase, lowercase, numeric, and symbol characters.",
          similarityScore: 0.912,
        },
      ],
    };
  }

  if (q.includes("travel") || q.includes("per diem") || q.includes("expense") || q.includes("hotel") || q.includes("meal")) {
    return {
      answer:
        "Under the Corporate Travel & Expense Allowance (v2, effective March 1, 2024), the domestic daily meal per diem has been adjusted to $80 per day. Business flights exceeding 4 hours may be booked in Premium Economy.",
      documentName: "Corporate Travel & Expense Allowance",
      version: 2,
      effectiveDate: "2024-03-01",
      department: "Finance & Operations",
      confidence: 0.928,
      responseTime: 210,
      conflict: true,
      conflictDetails: {
        currentVersion: "v2 (2024-03-01)",
        olderVersion: "v1 (2022-06-01)",
        conflictDescription:
          "Version 1 set the daily per diem to $50/day and restricted all flights to Economy. Version 2 increased allowance to $80/day.",
        reasonSelected:
          "Version 2 was resolved as current via most recent effective date (March 1, 2024).",
      },
      warning:
        "Version conflict resolved: Older $50/day rule was ignored in favor of current $80/day rate.",
      retrievedChunks: [
        {
          id: "chk-3",
          documentName: "Corporate Travel & Expense Allowance",
          versionNumber: 2,
          effectiveDate: "2024-03-01",
          department: "Finance & Operations",
          chunkText:
            "Section 2.3: Per Diem Meals and Incidentals. Employees on overnight travel receive a flat $80/day per diem allowance without itemized receipts required for food.",
          similarityScore: 0.928,
        },
      ],
    };
  }

  if (q.includes("leave") || q.includes("pto") || q.includes("vacation") || q.includes("holiday")) {
    return {
      answer:
        "According to the Employee Annual Leave Guidelines (v3, effective June 1, 2024), full-time employees accrue 22 days of paid time off per year, plus 12 statutory public holidays. Up to 5 unused PTO days may roll over to the following fiscal year.",
      documentName: "Employee Annual Leave & PTO Guidelines",
      version: 3,
      effectiveDate: "2024-06-01",
      department: "Human Resources",
      confidence: 0.956,
      responseTime: 195,
      conflict: false,
      retrievedChunks: [
        {
          id: "chk-5",
          documentName: "Employee Annual Leave & PTO Guidelines",
          versionNumber: 3,
          effectiveDate: "2024-06-01",
          department: "Human Resources",
          chunkText:
            "Section 1.2: Annual Leave Accrual. Full-time team members accrue 1.83 days per completed month (22 days annually). Rollover cap is set to 5 business days.",
          similarityScore: 0.956,
        },
      ],
    };
  }

  // General default response
  return {
    answer: `Based on your enterprise knowledge base documents, here is the verified current policy: The relevant guidelines mandate compliance with all updated organizational standards. Please consult the active departmental documentation for operational specifics.`,
    documentName: "Enterprise Operations Standard",
    version: 1,
    effectiveDate: "2024-01-01",
    department: "Operations",
    confidence: 0.89,
    responseTime: 220,
    conflict: false,
    retrievedChunks: [
      {
        id: "chk-gen",
        documentName: "Enterprise Operations Standard",
        versionNumber: 1,
        effectiveDate: "2024-01-01",
        department: "Operations",
        chunkText:
          "All corporate procedures must adhere to established governance protocols and receive annual executive review.",
        similarityScore: 0.89,
      },
    ],
  };
}
