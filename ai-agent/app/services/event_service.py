from app.agents.recover_agent import RecoveryAgent


class EventService:

    def __init__(self):
        self.agent = RecoveryAgent()

    def process_event(self, event):
        return self.agent.analyze_event(event)