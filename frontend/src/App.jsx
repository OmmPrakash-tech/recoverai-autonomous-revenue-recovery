import { useEffect, useMemo, useState } from "react";
import "./App.css";

function App() {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [selectedEvent, setSelectedEvent] = useState(null);

  const [search, setSearch] = useState("");
  const [priorityFilter, setPriorityFilter] = useState("ALL");
  const [policyFilter, setPolicyFilter] = useState("ALL");
  const [statusFilter, setStatusFilter] = useState("ALL");

  /* =========================================================
     FETCH EVENTS
     ========================================================= */

  const fetchEvents = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await fetch("/api/v1/revenue/events");

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }

      const data = await response.json();

      setEvents(data);
    } catch (err) {
      console.error(err);
      setError("Unable to load recovery events.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEvents();
  }, []);

  /* =========================================================
     STATISTICS
     ========================================================= */

  const stats = useMemo(() => {
    return {
      total: events.length,

      approved: events.filter(
        (event) => event.policyDecision === "ALLOWED"
      ).length,

      blocked: events.filter(
        (event) => event.policyDecision === "BLOCKED"
      ).length,

      manualReview: events.filter(
        (event) => event.status === "MANUAL_REVIEW_REQUIRED"
      ).length,
    };
  }, [events]);

  /* =========================================================
     FILTERING
     ========================================================= */

  const filteredEvents = useMemo(() => {
    return events.filter((event) => {
      const searchText = search.toLowerCase().trim();

      const matchesSearch =
        !searchText ||
        event.eventId?.toLowerCase().includes(searchText) ||
        event.customerId?.toLowerCase().includes(searchText) ||
        event.razorpayPaymentId?.toLowerCase().includes(searchText);

      const matchesPriority =
        priorityFilter === "ALL" ||
        event.priority === priorityFilter;

      const matchesPolicy =
        policyFilter === "ALL" ||
        event.policyDecision === policyFilter;

      const matchesStatus =
        statusFilter === "ALL" ||
        event.status === statusFilter;

      return (
        matchesSearch &&
        matchesPriority &&
        matchesPolicy &&
        matchesStatus
      );
    });
  }, [
    events,
    search,
    priorityFilter,
    policyFilter,
    statusFilter,
  ]);

  /* =========================================================
     STATUS CLASS
     ========================================================= */

  const getStatusClass = (status) => {
    switch (status) {
      case "RETRY_EXECUTED":
        return "status-success";

      case "RETRY_SCHEDULED":
        return "status-info";

      case "MANUAL_REVIEW_REQUIRED":
      case "CUSTOMER_ACTION_REQUIRED":
        return "status-warning";

      case "HUMAN_APPROVAL_REQUIRED":
        return "status-danger";

      case "ESCALATED_TO_SUPPORT":
        return "status-purple";

      case "ACTION_NOT_SUPPORTED":
        return "status-neutral";

      default:
        return "";
    }
  };

  /* =========================================================
     CLEAR FILTERS
     ========================================================= */

  const clearFilters = () => {
    setSearch("");
    setPriorityFilter("ALL");
    setPolicyFilter("ALL");
    setStatusFilter("ALL");
  };

  const approveRecovery = async (eventId) => {
  try {
    const response = await fetch(
      `/api/v1/revenue/events/${eventId}/approve`,
      {
        method: "POST",
      }
    );

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }

    const updatedEvent = await response.json();

    // Update the event in the table immediately
    setEvents((currentEvents) =>
      currentEvents.map((event) =>
        event.id === updatedEvent.id
          ? updatedEvent
          : event
      )
    );

    // Update modal data
    setSelectedEvent(updatedEvent);

  } catch (err) {
    console.error(err);
    setError("Unable to approve recovery action.");
  }
};

  const hasActiveFilters =
    search ||
    priorityFilter !== "ALL" ||
    policyFilter !== "ALL" ||
    statusFilter !== "ALL";

  /* =========================================================
     RENDER
     ========================================================= */

  return (
    <div className="app">

      {/* =====================================================
          HEADER
          ===================================================== */}

      <header className="header">

        <div className="brand">

          <div className="brand-icon">
            R
          </div>

          <div className="brand-copy">
            <h1>RecoverAI</h1>
            <p>Autonomous Revenue Recovery</p>
          </div>

        </div>

        <button
          className="refresh-button"
          onClick={fetchEvents}
          disabled={loading}
        >
          <span className="refresh-icon">↻</span>

          {loading ? "Refreshing..." : "Refresh"}
        </button>

      </header>


      {/* =====================================================
          MAIN
          ===================================================== */}

      <main className="container">

        {/* PAGE HEADING */}

        <section className="page-heading">

          <div>
            <div className="eyebrow">
              REVENUE OPERATIONS
            </div>

            <h2>
              Recovery Dashboard
            </h2>

            <p>
              Monitor payment failures, AI decisions and
              policy guardrails.
            </p>
          </div>

          <div className="live-status">
            <span className="live-dot"></span>
            System Online
          </div>

        </section>


        {/* ===================================================
            STATISTICS
            =================================================== */}

        <section className="stats-grid">

          <div className="stat-card">

            <div className="stat-top">
              <span className="stat-label">
                Total Events
              </span>

              <span className="stat-icon blue">
                ◈
              </span>
            </div>

            <strong>
              {stats.total}
            </strong>

            <span className="stat-description">
              Recovery events processed
            </span>

          </div>


          <div className="stat-card">

            <div className="stat-top">
              <span className="stat-label">
                Auto Approved
              </span>

              <span className="stat-icon green">
                ✓
              </span>
            </div>

            <strong>
              {stats.approved}
            </strong>

            <span className="stat-description">
              Allowed by policy
            </span>

          </div>


          <div className="stat-card">

            <div className="stat-top">
              <span className="stat-label">
                Blocked
              </span>

              <span className="stat-icon red">
                !
              </span>
            </div>

            <strong>
              {stats.blocked}
            </strong>

            <span className="stat-description">
              Require human approval
            </span>

          </div>


          <div className="stat-card">

            <div className="stat-top">
              <span className="stat-label">
                Manual Review
              </span>

              <span className="stat-icon yellow">
                ◷
              </span>
            </div>

            <strong>
              {stats.manualReview}
            </strong>

            <span className="stat-description">
              Require investigation
            </span>

          </div>

        </section>


        {/* ===================================================
            EVENTS
            =================================================== */}

        <section className="events-card">

          {/* EVENT HEADER */}

          <div className="events-header">

            <div>

              <div className="section-label">
                RECOVERY OPERATIONS
              </div>

              <div className="events-title-row">

                <h2>
                  Recovery Events
                </h2>

                <span className="event-count">
                  {filteredEvents.length} of {events.length}
                </span>

              </div>

              <p>
                Inspect payment recovery decisions and
                AI-generated actions.
              </p>

            </div>

          </div>


          {/* =================================================
              FILTER BAR
              ================================================= */}

          <div className="filters">

            {/* SEARCH */}

            <div className="search-wrapper">

              <span className="search-icon">
                ⌕
              </span>

              <input
                type="text"
                placeholder="Search Event ID, Customer ID or Payment ID..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />

              {search && (
                <button
                  className="search-clear"
                  onClick={() => setSearch("")}
                  aria-label="Clear search"
                >
                  ×
                </button>
              )}

            </div>


            {/* PRIORITY */}

            <select
              value={priorityFilter}
              onChange={(e) =>
                setPriorityFilter(e.target.value)
              }
            >
              <option value="ALL">
                All Priorities
              </option>

              <option value="critical">
                Critical
              </option>

              <option value="high">
                High
              </option>

              <option value="medium">
                Medium
              </option>
            </select>


            {/* POLICY */}

            <select
              value={policyFilter}
              onChange={(e) =>
                setPolicyFilter(e.target.value)
              }
            >
              <option value="ALL">
                All Policies
              </option>

              <option value="ALLOWED">
                Allowed
              </option>

              <option value="BLOCKED">
                Blocked
              </option>
            </select>


            {/* STATUS */}

            <select
              value={statusFilter}
              onChange={(e) =>
                setStatusFilter(e.target.value)
              }
            >
              <option value="ALL">
                All Statuses
              </option>

              <option value="RETRY_EXECUTED">
                Retry Executed
              </option>

              <option value="RETRY_SCHEDULED">
                Retry Scheduled
              </option>

              <option value="MANUAL_REVIEW_REQUIRED">
                Manual Review
              </option>

              <option value="HUMAN_APPROVAL_REQUIRED">
                Human Approval
              </option>

              <option value="ESCALATED_TO_SUPPORT">
                Escalated
              </option>

              <option value="CUSTOMER_ACTION_REQUIRED">
                Customer Action
              </option>

              <option value="ACTION_NOT_SUPPORTED">
                Action Not Supported
              </option>

            </select>


            {/* CLEAR */}

            <button
              className={`clear-filters ${
                hasActiveFilters ? "active" : ""
              }`}
              onClick={clearFilters}
            >
              Clear
            </button>

          </div>


          {/* ACTIVE FILTER INDICATOR */}

          {hasActiveFilters && (
            <div className="active-filter-bar">

              <span>
                Filters applied
              </span>

              <button onClick={clearFilters}>
                Clear all
              </button>

            </div>
          )}


          {/* =================================================
              LOADING
              ================================================= */}

          {loading && (
            <div className="message">
              <div className="loading-spinner"></div>

              <span>
                Loading recovery events...
              </span>
            </div>
          )}


          {/* =================================================
              ERROR
              ================================================= */}

          {error && (
            <div className="message error">

              <div className="message-icon">
                !
              </div>

              <span>
                {error}
              </span>

              <button onClick={fetchEvents}>
                Try again
              </button>

            </div>
          )}


          {/* =================================================
              NO EVENTS
              ================================================= */}

          {!loading &&
            !error &&
            events.length === 0 && (
              <div className="message">

                <div className="empty-icon">
                  ◌
                </div>

                <strong>
                  No recovery events
                </strong>

                <span>
                  No recovery events have been recorded yet.
                </span>

              </div>
            )}


          {/* =================================================
              NO FILTER RESULTS
              ================================================= */}

          {!loading &&
            !error &&
            events.length > 0 &&
            filteredEvents.length === 0 && (
              <div className="message">

                <div className="empty-icon">
                  ⌕
                </div>

                <strong>
                  No matching events
                </strong>

                <span>
                  Try adjusting your search or filters.
                </span>

                <button
                  className="message-action"
                  onClick={clearFilters}
                >
                  Clear filters
                </button>

              </div>
            )}


          {/* =================================================
              TABLE
              ================================================= */}

          {!loading &&
            !error &&
            filteredEvents.length > 0 && (

              <div className="table-wrapper">

                <table>

                  <thead>

                    <tr>

                      <th>
                        Event ID
                      </th>

                      <th>
                        Amount
                      </th>

                      <th>
                        Priority
                      </th>

                      <th>
                        AI Action
                      </th>

                      <th>
                        Policy
                      </th>

                      <th>
                        Gate
                      </th>

                      <th>
                        Status
                      </th>

                    </tr>

                  </thead>


                  <tbody>

                    {filteredEvents.map((event) => (

                      <tr
                        key={event.id}
                        className={
                          selectedEvent?.id === event.id
                            ? "selected-row"
                            : ""
                        }
                        onClick={() =>
                          setSelectedEvent(event)
                        }
                      >

                        {/* EVENT ID */}

                        <td className="event-id">

                          <button
                            className="event-link"
                            onClick={(e) => {
                              e.stopPropagation();
                              setSelectedEvent(event);
                            }}
                          >

                            <span className="event-id-icon">
                              ▣
                            </span>

                            <span>
                              {event.eventId}
                            </span>

                          </button>

                        </td>


                        {/* AMOUNT */}

                        <td className="amount-cell">
                          ₹
                          {Number(
                            event.amount
                          ).toLocaleString("en-IN")}
                        </td>


                        {/* PRIORITY */}

                        <td>

                          <span
                            className={`priority ${
                              event.priority || ""
                            }`}
                          >
                            {event.priority || "—"}
                          </span>

                        </td>


                        {/* AI ACTION */}

                        <td className="action-cell">
                          {event.recommendedAction || "—"}
                        </td>


                        {/* POLICY */}

                        <td>

                          {event.policyDecision ? (

                            <span
                              className={`badge ${
                                event.policyDecision ===
                                "ALLOWED"
                                  ? "allowed"
                                  : "blocked"
                              }`}
                            >

                              <span className="badge-dot"></span>

                              {event.policyDecision}

                            </span>

                          ) : (
                            "—"
                          )}

                        </td>


                        {/* GATE */}

                        <td className="gate-cell">
                          {event.gateStatus || "—"}
                        </td>


                        {/* STATUS */}

                        <td>

                          <span
                            className={`status ${getStatusClass(
                              event.status
                            )}`}
                          >
                            {event.status}
                          </span>

                        </td>

                      </tr>

                    ))}

                  </tbody>

                </table>

              </div>

            )}

        </section>

      </main>


      {/* =====================================================
          EVENT DETAILS MODAL
          ===================================================== */}

      {selectedEvent && (

        <div
          className="modal-overlay"
          onClick={() => setSelectedEvent(null)}
        >

          <div
            className="modal"
            onClick={(e) => e.stopPropagation()}
          >

            {/* MODAL HEADER */}

            <div className="modal-header">

              <div>

                <div className="modal-eyebrow">
                  RECOVERY EVENT
                </div>

                <div className="modal-title-row">

                  <h2>
                    {selectedEvent.eventId}
                  </h2>

                  <span
                    className={`modal-status ${getStatusClass(
                      selectedEvent.status
                    )}`}
                  >
                    {selectedEvent.status}
                  </span>

                </div>

              </div>

              <button
                className="modal-close"
                onClick={() => setSelectedEvent(null)}
                aria-label="Close"
              >
                ×
              </button>

            </div>


            {/* MODAL DETAILS */}

            <div className="details-grid">

              <div className="detail-item">
                <span>Payment ID</span>

                <strong>
                  {selectedEvent.razorpayPaymentId || "—"}
                </strong>
              </div>


              <div className="detail-item">
                <span>Customer ID</span>

                <strong>
                  {selectedEvent.customerId || "—"}
                </strong>
              </div>


              <div className="detail-item">
                <span>Amount</span>

                <strong className="detail-amount">
                  ₹
                  {Number(
                    selectedEvent.amount
                  ).toLocaleString("en-IN")}
                </strong>
              </div>


              <div className="detail-item">
                <span>Currency</span>

                <strong>
                  {selectedEvent.currency || "—"}
                </strong>
              </div>


              <div className="detail-item">
                <span>Failure Reason</span>

                <strong>
                  {selectedEvent.failureReason || "—"}
                </strong>
              </div>


              <div className="detail-item">
                <span>AI Recommended Action</span>

                <strong className="action-highlight">
                  {selectedEvent.recommendedAction || "—"}
                </strong>
              </div>


              <div className="detail-item">
                <span>Priority</span>

                <strong>
                  {selectedEvent.priority || "—"}
                </strong>
              </div>


              <div className="detail-item">
                <span>Policy Decision</span>

                <strong
                  className={
                    selectedEvent.policyDecision ===
                    "ALLOWED"
                      ? "text-success"
                      : selectedEvent.policyDecision ===
                        "BLOCKED"
                      ? "text-danger"
                      : ""
                  }
                >
                  {selectedEvent.policyDecision || "—"}
                </strong>
              </div>


              <div className="detail-item">
                <span>Gate Status</span>

                <strong>
                  {selectedEvent.gateStatus || "—"}
                </strong>
              </div>


              <div className="detail-item">
                <span>Final Status</span>

                <strong>
                  {selectedEvent.status || "—"}
                </strong>
              </div>

            </div>


            {/* DECISION REASON */}

            <div className="decision-box">

              <div className="decision-label">
                DECISION REASON
              </div>

              <p>
                {selectedEvent.decisionReason ||
                  "No policy decision was recorded for this event."}
              </p>

            </div>


            {/* CLOSE */}

            <div className="modal-actions">

  {selectedEvent.status === "HUMAN_APPROVAL_REQUIRED" && (
    <button
      className="approve-button"
      onClick={() => approveRecovery(selectedEvent.eventId)}
    >
      ✓ Approve Recovery
    </button>
  )}

  <button
    className="modal-close-button"
    onClick={() => setSelectedEvent(null)}
  >
    Close
  </button>

</div>

          </div>

        </div>

      )}

    </div>
  );
}

export default App;