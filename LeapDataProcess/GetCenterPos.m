function out = GetCenterPos(currTime, pos, edges, stdEdge, angle)
% Get the Center Position with given P1~3.
% Return [time x y z].

[row col] = size(pos);
% pos: id1 x1 y1 z1 id2 x2 y2 z2 id3 x3 y3 z3

edges = sortrows(edges')';
stdLen = stdEdge(4);

centerPos = zeros(row, 4);
num = 0;
for r = 1:row
    if pos(r, 1) == 0 && pos(r, 5) == 1 && pos(r, 9) == 2
        num = num + 1;
        
        % Naive method
        ratios = edges(r, :) ./ stdEdge(1, 1:3);
        ratio = mean(ratios);
        len2 = (stdLen * ratio) ^ 2;
        x0 = pos(r, 2:4) + [10, 10, 10];
        [X, FV, EF, OUTPUT] = fsolve(@(x)getDistance(x, pos(r, :), len2), x0);
        
        centerPos(num, 1) = currTime(r);
        centerPos(num, 2:4) = X;
        % out(r, 2:4) = X;
    end
end
out = centerPos(1:num, :);
end

function out = getDistance(x, pos, len2)
    out = zeros(3);
    for i = 1:3
       out(i) = sum((x - pos(1, (i*4-2):(i*4))) .^ 2) - len2;
    end
end