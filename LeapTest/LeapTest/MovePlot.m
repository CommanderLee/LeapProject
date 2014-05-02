% Plot the trace data of Leap C++ project.
% LI ZHEN, April 12th, 2014.

for i = 70:70
    filename = sprintf('ToolMove%d.csv', i);
    % time id1 x1 y1 z1 id2 x2 y2 z2 id3 x3 y3 z3
    mat = csvread(filename, 1, 0);
%     figure;
%     scatter3(mat(:, 3), mat(:, 4), mat(:, 5), '.');
%     title(filename);
    figure;
    
    fprintf('%d:\n', i);
    [row col] = size(mat);
%     plot3(mat(:, 3), mat(:, 4), mat(:, 5), '.r');
%     hold on;
%     plot3(mat(:, 7), mat(:, 8), mat(:, 9), '.b');
%     hold on;
%     plot3(mat(:, 11), mat(:, 12), mat(:, 13), '.k');
    for r=1:row
        plot3(mat(r, 3), mat(r, 4), mat(r, 5), '.r');
        grid on;
        hold on;
        if mat(r, 6) == 1
            plot3(mat(r, 7), mat(r, 8), mat(r, 9), '.b');
            hold on;
            if mat(r, 10) == 2
                plot3(mat(r, 11), mat(r, 12), mat(r, 13), '.c');
            end
        end
    end
end
