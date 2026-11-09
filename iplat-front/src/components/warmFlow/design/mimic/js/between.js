import {BaseNodeModel} from "@/components/warmFlow/design/mimic/js/baseNodeModel";
import {BaseNodeView} from "@/components/warmFlow/design/mimic/js/baseNodeView";

class BetweenModel extends BaseNodeModel {}

class BetweenView extends BaseNodeView {}

export default {
  type: "between",
  model: BetweenModel,
  view: BetweenView,
};
